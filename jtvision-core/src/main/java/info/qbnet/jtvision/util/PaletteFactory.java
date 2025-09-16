package info.qbnet.jtvision.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory responsible for constructing {@link TPalette} instances using either
 * built-in defaults or optional JSON configuration files located in
 * {@code src/main/resources/palettes}.
 */
public final class PaletteFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PaletteFactory.class);
    private static final String RESOURCE_PREFIX = "palettes/";
    private static final Map<String, PaletteDefinition<?>> DEFAULTS = new ConcurrentHashMap<>();
    private static final Map<String, TPalette> CACHE = new ConcurrentHashMap<>();

    private PaletteFactory() {
    }

    /**
     * Registers the default palette data for the specified palette name using a
     * hexadecimal string representation identical to the one previously used by
     * {@link TPalette#mapFromHexString(String, Enum[])}.
     *
     * @param name      palette identifier
     * @param roleEnum  enum describing the palette roles
     * @param hexValues hexadecimal palette values (e.g. "\\x0A\\x0B")
     */
    public static synchronized <R extends Enum<R> & PaletteRole> void registerDefaults(String name,
                                                                                       Class<R> roleEnum,
                                                                                       String hexValues) {
        Objects.requireNonNull(hexValues, "hexValues");
        registerDefaults(name, roleEnum, TPalette.parseHexString(hexValues));
    }

    /**
     * Registers the default palette data for the specified palette name.
     *
     * @param name     palette identifier
     * @param roleEnum enum describing the palette roles
     * @param values   palette values (one entry for each enum constant)
     */
    public static synchronized <R extends Enum<R> & PaletteRole> void registerDefaults(String name,
                                                                                       Class<R> roleEnum,
                                                                                       byte[] values) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(roleEnum, "roleEnum");
        Objects.requireNonNull(values, "values");

        R[] constants = roleEnum.getEnumConstants();
        if (constants == null || constants.length == 0) {
            throw new IllegalArgumentException("Palette role enum has no constants: " + roleEnum);
        }
        if (constants.length != values.length) {
            throw new IllegalArgumentException("Palette default value count mismatch for " + roleEnum
                    + ": expected " + constants.length + " entries but found " + values.length);
        }

        PaletteDefinition<R> definition = new PaletteDefinition<>(roleEnum, values.clone());
        DEFAULTS.put(name, definition);
        CACHE.remove(name);
    }

    /**
     * Returns a palette by name. The palette is constructed using the registered
     * defaults and optionally overridden by JSON configuration when available.
     *
     * @param name palette identifier
     * @return palette instance
     */
    public static TPalette get(String name) {
        Objects.requireNonNull(name, "name");
        PaletteDefinition<?> definition = DEFAULTS.get(name);
        if (definition == null) {
            throw new IllegalArgumentException("Palette '" + name + "' has not been registered");
        }
        return CACHE.computeIfAbsent(name, key -> buildPalette(key, definition));
    }

    private static TPalette buildPalette(String name, PaletteDefinition<?> definition) {
        Map<String, Byte> overrides = loadOverrides(name);
        return definition.toPalette(overrides);
    }

    private static Map<String, Byte> loadOverrides(String name) {
        String resource = RESOURCE_PREFIX + name + ".json";
        try (InputStream in = PaletteFactory.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                LOG.debug("Palette resource {} not found; using defaults for {}", resource, name);
                return Collections.emptyMap();
            }
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Byte> overrides = parseOverrides(name, json);
            if (overrides.isEmpty()) {
                LOG.debug("Palette {} loaded with defaults from {}", name, resource);
            } else {
                LOG.info("Loaded {} override{} for palette {} from {}", overrides.size(),
                        overrides.size() == 1 ? "" : "s", name, resource);
            }
            return overrides;
        } catch (IOException e) {
            LOG.warn("Error reading palette resource {}: {}", resource, e.getMessage());
            return Collections.emptyMap();
        }
    }

    private static Map<String, Byte> parseOverrides(String paletteName, String json) {
        if (json == null) {
            return Collections.emptyMap();
        }
        Map<String, Byte> overrides = new LinkedHashMap<>();
        int index = 0;
        int length = json.length();
        while (index < length) {
            char ch = json.charAt(index);
            if (Character.isWhitespace(ch) || ch == '{' || ch == ',') {
                index++;
                continue;
            }
            if (ch == '}') {
                break;
            }
            if (ch != '"') {
                index++;
                continue;
            }
            int keyEnd = json.indexOf('"', index + 1);
            if (keyEnd < 0) {
                break;
            }
            String key = json.substring(index + 1, keyEnd);
            index = keyEnd + 1;
            index = skipWhitespace(json, index);
            if (index >= length || json.charAt(index) != ':') {
                throw new IllegalArgumentException("Invalid JSON palette definition: missing ':' after key " + key);
            }
            index++;
            index = skipWhitespace(json, index);
            if (index >= length) {
                break;
            }
            String rawValue;
            char valueStart = json.charAt(index);
            if (valueStart == '"') {
                int valueEnd = index + 1;
                while (true) {
                    valueEnd = json.indexOf('"', valueEnd);
                    if (valueEnd < 0) {
                        throw new IllegalArgumentException("Unterminated string value for key " + key);
                    }
                    if (json.charAt(valueEnd - 1) != '\\') {
                        break;
                    }
                    valueEnd++;
                }
                rawValue = json.substring(index + 1, valueEnd);
                index = valueEnd + 1;
            } else {
                int valueEnd = index;
                while (valueEnd < length) {
                    char current = json.charAt(valueEnd);
                    if (current == ',' || current == '}') {
                        break;
                    }
                    valueEnd++;
                }
                rawValue = json.substring(index, valueEnd).trim();
                index = valueEnd;
            }
            if ("enum".equalsIgnoreCase(key) || "palette".equalsIgnoreCase(key) || rawValue.isEmpty()) {
                continue;
            }
            try {
                Byte value = parseByteValue(rawValue);
                overrides.put(key.toUpperCase(Locale.ROOT), value);
            } catch (IllegalArgumentException ex) {
                LOG.warn("Palette {} entry '{}' has invalid value '{}'; falling back to default", paletteName, key,
                        rawValue, ex);
            }
        }
        return overrides;
    }

    private static int skipWhitespace(String text, int index) {
        int pos = index;
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    private static byte parseByteValue(String raw) {
        String value = raw.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("empty value");
        }
        int radix = 10;
        if (value.startsWith("\\x")) {
            value = value.substring(2);
            radix = 16;
        } else if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
            radix = 16;
        } else if (value.startsWith("#")) {
            value = value.substring(1);
            radix = 16;
        } else if (value.matches("[0-9A-Fa-f]{2}")) {
            radix = 16;
        } else if (value.matches("\\d+")) {
            radix = 10;
        } else {
            throw new IllegalArgumentException("unsupported format: " + raw);
        }
        int parsed = Integer.parseInt(value, radix);
        return toByte(parsed);
    }

    private static byte toByte(int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("value out of range: " + value);
        }
        return (byte) value;
    }

    /**
     * Returns a snapshot of all registered default palettes. The map keys are
     * palette identifiers and values represent role-to-byte mappings using the
     * enum constant names. The returned structure is read-only.
     */
    public static Map<String, Map<String, Byte>> snapshotDefaults() {
        Map<String, Map<String, Byte>> snapshot = new TreeMap<>();
        DEFAULTS.forEach((name, definition) -> snapshot.put(name, definition.defaultMapping()));
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * Returns the set of registered palette names.
     */
    public static Set<String> registeredNames() {
        return Set.copyOf(DEFAULTS.keySet());
    }

    private static final class PaletteDefinition<R extends Enum<R> & PaletteRole> {
        private final Class<R> roleEnum;
        private final byte[] defaultValues;

        PaletteDefinition(Class<R> roleEnum, byte[] defaultValues) {
            this.roleEnum = roleEnum;
            this.defaultValues = defaultValues;
        }

        Map<String, Byte> defaultMapping() {
            R[] constants = roleEnum.getEnumConstants();
            if (constants == null) {
                throw new IllegalStateException("Palette role enum has no constants: " + roleEnum);
            }
            if (constants.length != defaultValues.length) {
                throw new IllegalStateException("Palette defaults and role count mismatch for " + roleEnum);
            }
            Map<String, Byte> map = new LinkedHashMap<>();
            for (int i = 0; i < constants.length; i++) {
                map.put(constants[i].name(), defaultValues[i]);
            }
            return Collections.unmodifiableMap(map);
        }

        TPalette toPalette(Map<String, Byte> overrides) {
            R[] constants = roleEnum.getEnumConstants();
            if (constants == null) {
                throw new IllegalStateException("Palette role enum has no constants: " + roleEnum);
            }
            if (constants.length != defaultValues.length) {
                throw new IllegalStateException("Palette defaults and role count mismatch for " + roleEnum);
            }
            EnumMap<R, Byte> map = new EnumMap<>(roleEnum);
            for (int i = 0; i < constants.length; i++) {
                byte value = defaultValues[i];
                if (overrides != null && !overrides.isEmpty()) {
                    Byte override = overrides.get(constants[i].name());
                    if (override != null) {
                        value = override;
                    }
                }
                map.put(constants[i], value);
            }
            return new TPalette(map);
        }
    }
}
