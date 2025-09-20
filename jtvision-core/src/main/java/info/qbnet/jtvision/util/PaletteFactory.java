package info.qbnet.jtvision.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory responsible for constructing {@link TPalette} instances using JSON definitions located in
 * {@code src/main/resources/palettes}.
 */
public final class PaletteFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PaletteFactory.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String RESOURCE_PREFIX = "palettes/";
    private static final Map<String, PaletteDefinition<?>> DEFAULTS = new ConcurrentHashMap<>();
    private static final Map<String, TPalette> CACHE = new ConcurrentHashMap<>();

    /** Controls how missing palette entries are reported. */
    public enum MissingEntryPolicy {
        /** Log a warning and fall back to {@link info.qbnet.jtvision.views.TView#ERROR_ATTR}. */
        LOG,
        /** Throw an {@link IllegalStateException} when a palette entry is missing. */
        THROW
    }

    private static final String STRICT_PROPERTY = "jtvision.palette.strict";
    private static volatile MissingEntryPolicy missingEntryPolicy =
            Boolean.getBoolean(STRICT_PROPERTY) ? MissingEntryPolicy.THROW : MissingEntryPolicy.LOG;

    private PaletteFactory() {
    }

    /**
     * Returns the currently configured policy for handling missing palette entries.
     *
     * @return the active {@link MissingEntryPolicy}
     */
    public static MissingEntryPolicy getMissingEntryPolicy() {
        return missingEntryPolicy;
    }

    /**
     * Updates the policy controlling how missing palette entries are handled.
     *
     * @param policy desired policy, must not be {@code null}
     */
    public static void setMissingEntryPolicy(MissingEntryPolicy policy) {
        missingEntryPolicy = Objects.requireNonNull(policy, "policy");
    }

    /**
     * Registers the palette {@code name} by loading its entries from the JSON resource located in
     * {@code src/main/resources/palettes}.
     *
     * @param name     palette identifier
     * @param roleEnum enum describing the palette roles
     */
    public static synchronized <R extends Enum<R> & PaletteRole> void register(String name,
                                                                               Class<R> roleEnum) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(roleEnum, "roleEnum");

        EnumMap<R, Byte> entries = loadPalette(name, roleEnum);
        PaletteDefinition<R> definition = new PaletteDefinition<>(roleEnum, entries);
        DEFAULTS.put(name, definition);
        CACHE.remove(name);
    }

    /**
     * Returns a palette by name. The palette is constructed from the JSON resource registered for the
     * palette.
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
        return CACHE.computeIfAbsent(name, key -> definition.toPalette());
    }

    private static <R extends Enum<R> & PaletteRole> EnumMap<R, Byte> loadPalette(String name, Class<R> roleEnum) {
        R[] constants = roleEnum.getEnumConstants();
        if (constants == null || constants.length == 0) {
            throw new IllegalArgumentException("Palette role enum has no constants: " + roleEnum);
        }

        String resource = RESOURCE_PREFIX + name + ".json";
        try (InputStream in = PaletteFactory.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Palette resource '" + resource + "' not found");
            }

            JsonNode root;
            try {
                root = OBJECT_MAPPER.readTree(in);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        "Invalid JSON in palette resource '" + resource + "': " + e.getOriginalMessage(), e);
            }

            EnumMap<R, Byte> entries = parsePalette(name, roleEnum, root);
            LOG.info("Loaded palette {} from {}", name, resource);
            return entries;
        } catch (IOException e) {
            throw new IllegalStateException("Error reading palette resource '" + resource + "': " + e.getMessage(), e);
        }
    }

    private static <R extends Enum<R> & PaletteRole> EnumMap<R, Byte> parsePalette(String paletteName,
                                                                                   Class<R> roleEnum,
                                                                                   JsonNode root) {
        if (root == null || root.isNull()) {
            throw new IllegalStateException("Palette " + paletteName + " configuration is empty");
        }
        if (!root.isObject()) {
            throw new IllegalStateException("Palette " + paletteName + " configuration must be a JSON object");
        }

        Map<String, R> roleLookup = new HashMap<>();
        for (R constant : roleEnum.getEnumConstants()) {
            roleLookup.put(constant.name().toUpperCase(Locale.ROOT), constant);
        }

        EnumMap<R, Byte> entries = new EnumMap<>(roleEnum);
        root.fields().forEachRemaining(entry -> {
            String rawKey = entry.getKey();
            if (rawKey == null) {
                return;
            }
            R role = roleLookup.get(rawKey.toUpperCase(Locale.ROOT));
            if (role == null) {
                LOG.warn("Palette {} entry '{}' does not match any role; ignoring", paletteName, rawKey);
                return;
            }

            JsonNode valueNode = entry.getValue();
            if (valueNode == null || valueNode.isNull()) {
                throw new IllegalStateException(
                        "Palette " + paletteName + " role '" + rawKey + "' has null value");
            }

            String rawValue;
            if (valueNode.isTextual() || valueNode.isNumber()) {
                rawValue = valueNode.asText();
            } else {
                throw new IllegalStateException(
                        "Palette " + paletteName + " role '" + rawKey + "' has unsupported value type "
                                + valueNode.getNodeType());
            }

            Byte parsed;
            try {
                parsed = parseByteValue(rawValue);
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException(
                        "Palette " + paletteName + " role '" + rawKey + "' has invalid value '" + rawValue + "'",
                        ex);
            }
            entries.put(role, parsed);
        });

        for (R constant : roleEnum.getEnumConstants()) {
            if (!entries.containsKey(constant)) {
                throw new IllegalStateException(
                        "Palette " + paletteName + " is missing a value for role " + constant.name());
            }
        }
        return entries;
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
        return PaletteRole.toByte(parsed);
    }

    /**
     * Returns a snapshot of all registered palettes. The map keys are palette identifiers and values represent
     * role-to-byte mappings using the enum constant names. The returned structure is read-only.
     */
    public static Map<String, Map<String, Byte>> snapshotDefaults() {
        Map<String, Map<String, Byte>> snapshot = new TreeMap<>();
        DEFAULTS.forEach((name, definition) -> snapshot.put(name, definition.mapping()));
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
        private final EnumMap<R, Byte> values;

        PaletteDefinition(Class<R> roleEnum, EnumMap<R, Byte> values) {
            this.roleEnum = roleEnum;
            this.values = new EnumMap<>(values);
        }

        Map<String, Byte> mapping() {
            R[] constants = roleEnum.getEnumConstants();
            if (constants == null) {
                throw new IllegalStateException("Palette role enum has no constants: " + roleEnum);
            }
            Map<String, Byte> map = new LinkedHashMap<>();
            for (R constant : constants) {
                Byte value = values.get(constant);
                if (value == null) {
                    throw new IllegalStateException("Missing value for role " + constant + " in " + roleEnum);
                }
                map.put(constant.name(), value);
            }
            return Collections.unmodifiableMap(map);
        }

        TPalette toPalette() {
            return new TPalette(new EnumMap<>(values));
        }
    }
}
