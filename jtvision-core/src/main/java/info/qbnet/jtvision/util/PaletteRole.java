package info.qbnet.jtvision.util;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Identifies a symbolic role within a view palette. Roles are numbered using
 * the one-based indices used by Turbo Vision palettes so that legacy palette
 * definitions can be mapped directly onto the new enum based representation.
 */
public interface PaletteRole {

    /**
     * Returns the one-based palette index associated with this role.
     *
     * @return palette slot number (starting at 1)
     */
    default int index() {
        return defaultIndex();
    }

    /**
     * Returns the default palette index assigned to this role. Implementations
     * that do not override {@link #index()} can rely on this method to derive
     * indices from the declaration order of the enum constants. Existing enums
     * may override this method (or {@link #index()}) to supply explicit
     * numbering compatible with legacy layouts.
     */
    default int defaultIndex() {
        if (this instanceof Enum<?> constant) {
            return constant.ordinal() + 1;
        }
        throw new UnsupportedOperationException("Automatic palette indices require enum constants");
    }

    /**
     * Returns the default palette value assigned to this role.
     *
     * @return palette entry value represented as an unsigned byte
     */
    byte defaultValue();

    /**
     * Determines whether {@code role} declares a custom palette index via
     * {@link #index()} or {@link #defaultIndex()}.
     */
    static boolean hasExplicitIndex(PaletteRole role) {
        Objects.requireNonNull(role, "role");
        Class<?> type = role.getClass();
        while (type != null && type != Enum.class && type != Object.class) {
            if (declaresIndexMethod(type)) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }

    private static boolean declaresIndexMethod(Class<?> type) {
        for (Method method : type.getDeclaredMethods()) {
            if (method.getParameterCount() == 0) {
                String name = method.getName();
                if ("index".equals(name) || "defaultIndex".equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Utility method ensuring that palette values remain within the
     * unsigned-byte range accepted by Turbo Vision palettes.
     */
    static byte toByte(int value) {
        if (value < 0 || value > 0xFF) {
            throw new IllegalArgumentException("Palette value out of range: " + value);
        }
        return (byte) value;
    }
}
