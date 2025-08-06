package info.qbnet.jtvision.util;

import java.awt.Color;

/**
 * Utility for converting Turbo Vision color attributes into AWT colors.
 *
 * <p>Color attribute layout (bits in a byte):
 * <ul>
 *   <li>0-3: foreground color (0-15)</li>
 *   <li>4-6: background color (0-7)</li>
 *   <li>7:   background intensity bit</li>
 * </ul>
 * </p>
 */
public final class DosPalette {
    private DosPalette() {
    }

    /**
     * Returns the foreground {@link Color} for the given attribute byte.
     */
    public static Color getForeground(int attribute) {
        return DosColor.fromIndex(attribute & 0x0F);
    }

    /**
     * Returns the background {@link Color} for the given attribute byte.
     */
    public static Color getBackground(int attribute) {
        int backgroundIndex = (attribute >> 4) & 0x07;
        if ((attribute & 0x80) != 0) {
            backgroundIndex |= 0x08;
        }
        return DosColor.fromIndex(backgroundIndex);
    }
}

