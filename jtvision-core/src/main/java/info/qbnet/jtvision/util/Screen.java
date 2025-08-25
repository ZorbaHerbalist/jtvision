package info.qbnet.jtvision.util;

import java.awt.Color;

/**
 * Compatibility wrapper that extends {@link Buffer}.
 */
public class Screen extends Buffer {

    public Screen(int width, int height, Color defaultForeground, Color defaultBackground) {
        super(width, height, defaultForeground, defaultBackground);
    }

    public Screen(int width, int height) {
        super(width, height);
    }
}

