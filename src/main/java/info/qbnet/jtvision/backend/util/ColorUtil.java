package info.qbnet.jtvision.backend.util;

import javafx.scene.paint.Color;

/**
 * Utility methods for converting between AWT colors and backend-specific color classes.
 */
public final class ColorUtil {
    private ColorUtil() {
    }

    /**
     * Convert a Swing Color to a JavaFX Color.
     */
    public static Color toFx(java.awt.Color c) {
        return Color.rgb(c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Convert a Swing Color to a LibGDX Color.
     */
    public static com.badlogic.gdx.graphics.Color toGdx(java.awt.Color c) {
        return new com.badlogic.gdx.graphics.Color(
                c.getRed() / 255f,
                c.getGreen() / 255f,
                c.getBlue() / 255f,
                1f);
    }
}
