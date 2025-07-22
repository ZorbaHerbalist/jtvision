package info.qbnet.jtvision;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.backend.Screen;

import java.awt.*;

/**
 * Console class provides a text-mode interface for writing to a screen buffer.
 */
public class Console {

    private final Screen screenBuffer;
    private final Backend backend;

    /**
     * Constructs a Console with the given screen buffer and rendering backend.
     * @param screenBuffer the screen buffer
     * @param backend the rendering backend
     */
    public Console(Screen screenBuffer, Backend backend) {
        this.screenBuffer = screenBuffer;
        this.backend = backend;
    }

    /**
     * Prints a string at the specified coordinates with specified colors.
     * @param x horizontal position
     * @param y vertical position
     * @param text the text to print
     * @param fg foreground color
     * @param bg background color
     */
    public void putString(int x, int y, String text, Color fg, Color bg) {
        if (text == null || fg == null || bg == null) {
            System.err.println("putString(): text or color arguments are null. Ignored.");
            return;
        }

        int maxLength = screenBuffer.getCols() - x;
        if (maxLength <= 0 || y < 0 || y >= screenBuffer.getRows()) {
            System.err.printf("putString(): coordinates out of bounds or no space to render at (%d,%d). Ignored.\n", x , y);
            return;
        }

        int len = Math.min(text.length(), maxLength);
        for (int i = 0; i < len; i++) {
            screenBuffer.setChar(x + i, y, text.charAt(i), fg, bg);
        }
        backend.render();
    }

    /**
     * Clears the screen buffer and renders the empty screen.
     */
    public void clearScreen() {
        screenBuffer.clear();
        backend.render();
    }

}
