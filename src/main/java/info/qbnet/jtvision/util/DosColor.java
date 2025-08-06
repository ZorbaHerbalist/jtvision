package info.qbnet.jtvision.util;

import java.awt.Color;

/**
 * Enumeration of the 16-color DOS palette used by Turbo Vision.
 * Each value maps to the RGB intensities of the standard VGA palette.
 */
public enum DosColor {
    BLACK(0x00, 0x00, 0x00),
    BLUE(0x00, 0x00, 0xAA),
    GREEN(0x00, 0xAA, 0x00),
    CYAN(0x00, 0xAA, 0xAA),
    RED(0xAA, 0x00, 0x00),
    MAGENTA(0xAA, 0x00, 0xAA),
    BROWN(0xAA, 0x55, 0x00),
    LIGHT_GRAY(0xAA, 0xAA, 0xAA),
    DARK_GRAY(0x55, 0x55, 0x55),
    LIGHT_BLUE(0x55, 0x55, 0xFF),
    LIGHT_GREEN(0x55, 0xFF, 0x55),
    LIGHT_CYAN(0x55, 0xFF, 0xFF),
    LIGHT_RED(0xFF, 0x55, 0x55),
    LIGHT_MAGENTA(0xFF, 0x55, 0xFF),
    YELLOW(0xFF, 0xFF, 0x55),
    WHITE(0xFF, 0xFF, 0xFF);

    private final Color color;

    DosColor(int r, int g, int b) {
        this.color = new Color(r, g, b);
    }

    /**
     * Returns the AWT {@link Color} corresponding to this DOS palette entry.
     */
    public Color toAwt() {
        return color;
    }

    /**
     * Looks up the AWT {@link Color} by the given DOS palette index.
     *
     * @param index index in the range 0-15
     * @return corresponding {@link Color}
     */
    public static Color fromIndex(int index) {
        return values()[index & 0x0F].toAwt();
    }
}

