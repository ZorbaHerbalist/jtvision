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
import java.util.function.Function;

/**
 * Factory responsible for constructing {@link TPalette} instances using either
 * built-in defaults or optional JSON configuration files located in
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
     * Registers the default palette data for the specified palette name using
     * the {@link PaletteRole#defaultValue()} definitions provided by the enum
     * constants.
     */
    public static synchronized <R extends Enum<R> & PaletteRole> void registerDefaults(String name,
                                                                                       Class<R> roleEnum) {
        registerDefaults(name, roleEnum, PaletteRole::defaultValue);
    }

    /**
     * Registers the default palette data for the specified palette name using
     * the values provided by {@code defaultMapper}.
     *
     * @param name           palette identifier
     * @param roleEnum       enum describing the palette roles
     * @param defaultMapper  function returning the default value for a given
     *                       role
     */
    public static synchronized <R extends Enum<R> & PaletteRole> void registerDefaults(String name,
                                                                                       Class<R> roleEnum,
                                                                                       Function<R, Byte> defaultMapper) {
        registerPalette(name, roleEnum, defaultMapper, false);
    }

    /**
     * Registers a palette whose indices are derived automatically from the enum
     * declaration order. The supplied enum must not override
     * {@link PaletteRole#index()} or {@link PaletteRole#defaultIndex()}.
     */
    public static synchronized <R extends Enum<R> & PaletteRole> void registerAutoIndexed(String name,
                                                                                          Class<R> roleEnum) {
        registerAutoIndexed(name, roleEnum, PaletteRole::defaultValue);
    }

    /**
     * Registers a palette whose indices are derived automatically from the enum
     * declaration order using a custom default mapper.
     */
    public static synchronized <R extends Enum<R> & PaletteRole> void registerAutoIndexed(String name,
                                                                                          Class<R> roleEnum,
                                                                                          Function<R, Byte> defaultMapper) {
        registerPalette(name, roleEnum, defaultMapper, true);
    }

    private static <R extends Enum<R> & PaletteRole> void registerPalette(String name,
                                                                          Class<R> roleEnum,
                                                                          Function<R, Byte> defaultMapper,
                                                                          boolean enforceAutoIndexing) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(roleEnum, "roleEnum");
        Objects.requireNonNull(defaultMapper, "defaultMapper");

        R[] constants = roleEnum.getEnumConstants();
        if (constants == null || constants.length == 0) {
            throw new IllegalArgumentException("Palette role enum has no constants: " + roleEnum);
        }
        if (enforceAutoIndexing) {
            for (R constant : constants) {
                if (PaletteRole.hasExplicitIndex(constant)) {
                    throw new IllegalArgumentException(
                            "Palette role " + constant + " defines an explicit index; use registerDefaults instead");
                }
            }
        }
        EnumMap<R, Byte> defaults = new EnumMap<>(roleEnum);
        for (R constant : constants) {
            Byte value = defaultMapper.apply(constant);
            if (value == null) {
                throw new IllegalArgumentException("Palette default mapper returned null for role " + constant);
            }
            defaults.put(constant, value);
        }

        PaletteDefinition<R> definition = new PaletteDefinition<>(roleEnum, defaults);
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
        Map<String, Byte> overrides = loadOverrides(name, definition);
        return definition.toPalette(overrides);
    }

    private static Map<String, Byte> loadOverrides(String name, PaletteDefinition<?> definition) {
        String resource = RESOURCE_PREFIX + name + ".json";
        try (InputStream in = PaletteFactory.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                LOG.debug("Palette resource {} not found; using defaults for {}", resource, name);
                return Collections.emptyMap();
            }
            JsonNode root;
            try {
                root = OBJECT_MAPPER.readTree(in);
            } catch (JsonProcessingException e) {
                LOG.warn("Invalid JSON in palette resource {}: {}", resource, e.getOriginalMessage());
                return Collections.emptyMap();
            }
            Map<String, Byte> overrides = parseOverrides(name, definition, root);
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

    private static Map<String, Byte> parseOverrides(String paletteName, PaletteDefinition<?> definition,
                                                    JsonNode root) {
        if (root == null || root.isNull()) {
            return Collections.emptyMap();
        }
        if (!root.isObject()) {
            LOG.warn("Palette {} configuration is not a JSON object; ignoring overrides", paletteName);
            return Collections.emptyMap();
        }
        Map<String, String> roleLookup = new HashMap<>();
        Enum<?>[] constants = definition.roleEnum.getEnumConstants();
        if (constants != null) {
            for (Enum<?> constant : constants) {
                roleLookup.put(constant.name().toUpperCase(Locale.ROOT), constant.name());
            }
        }
        Map<String, Byte> overrides = new LinkedHashMap<>();
        root.fields().forEachRemaining(entry -> {
            String rawKey = entry.getKey();
            if (rawKey == null) {
                return;
            }
            String normalizedKey = rawKey.toUpperCase(Locale.ROOT);
            String roleName = roleLookup.get(normalizedKey);
            if (roleName == null) {
                LOG.warn("Palette {} entry '{}' does not match any role; ignoring", paletteName, rawKey);
                return;
            }
            JsonNode valueNode = entry.getValue();
            if (valueNode == null || valueNode.isNull()) {
                LOG.warn("Palette {} role '{}' has null value; falling back to default", paletteName, rawKey);
                return;
            }
            String rawValue;
            if (valueNode.isTextual() || valueNode.isNumber()) {
                rawValue = valueNode.asText();
            } else {
                LOG.warn("Palette {} role '{}' has unsupported value type {}; falling back to default", paletteName,
                        rawKey, valueNode.getNodeType());
                return;
            }
            try {
                Byte value = parseByteValue(rawValue);
                overrides.put(roleName, value);
            } catch (IllegalArgumentException ex) {
                LOG.warn("Palette {} role '{}' has invalid value '{}'; falling back to default", paletteName, rawKey,
                        rawValue, ex);
            }
        });
        return overrides;
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
        private final EnumMap<R, Byte> defaultValues;

        PaletteDefinition(Class<R> roleEnum, EnumMap<R, Byte> defaultValues) {
            this.roleEnum = roleEnum;
            this.defaultValues = new EnumMap<>(defaultValues);
        }

        Map<String, Byte> defaultMapping() {
            R[] constants = roleEnum.getEnumConstants();
            if (constants == null) {
                throw new IllegalStateException("Palette role enum has no constants: " + roleEnum);
            }
            Map<String, Byte> map = new LinkedHashMap<>();
            for (R constant : constants) {
                Byte value = defaultValues.get(constant);
                if (value == null) {
                    throw new IllegalStateException("Missing default value for role " + constant + " in " + roleEnum);
                }
                map.put(constant.name(), value);
            }
            return Collections.unmodifiableMap(map);
        }

        TPalette toPalette(Map<String, Byte> overrides) {
            R[] constants = roleEnum.getEnumConstants();
            if (constants == null) {
                throw new IllegalStateException("Palette role enum has no constants: " + roleEnum);
            }
            EnumMap<R, Byte> map = new EnumMap<>(roleEnum);
            for (R constant : constants) {
                Byte base = defaultValues.get(constant);
                if (base == null) {
                    throw new IllegalStateException("Missing default value for role " + constant + " in " + roleEnum);
                }
                byte value = base;
                if (overrides != null && !overrides.isEmpty()) {
                    Byte override = overrides.get(constant.name());
                    if (override != null) {
                        value = override;
                    }
                }
                map.put(constant, value);
            }
            return new TPalette(map);
        }
    }
}
