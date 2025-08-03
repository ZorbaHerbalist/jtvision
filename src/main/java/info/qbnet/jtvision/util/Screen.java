package info.qbnet.jtvision.util;

import java.awt.Color;

/**
 * Compatibility wrapper that extends {@link ArrayCharacterBuffer}.
 */
public class Screen extends ArrayCharacterBuffer {

    public Screen(int width, int height, Color defaultForeground, Color defaultBackground) {
        super(width, height, defaultForeground, defaultBackground);
    }

    public Screen(int width, int height) {
        super(width, height);
    }
}

