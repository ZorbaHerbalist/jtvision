package info.qbnet.jtvision.util;

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
    int index();

    /**
     * Returns the default palette value assigned to this role.
     *
     * @return palette entry value represented as an unsigned byte
     */
    byte defaultValue();

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
