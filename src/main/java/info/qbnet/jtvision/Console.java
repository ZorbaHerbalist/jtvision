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
        if (text == null) {
            System.err.println("putString(): text is null. Ignored.");
            return;
        }
        if (!screenBuffer.isValidColor(fg, bg)) {
            System.err.printf("putString(): null color argument at (%d,%d). Ignored.%n", x, y);
            return;
        }
        if (!screenBuffer.isInBounds(x, y)) {
            System.err.printf("putString(): coordinates out of bounds (%d,%d). Ignored.%n", x, y);
            return;
        }

        int maxLength = screenBuffer.getCols() - x;
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
