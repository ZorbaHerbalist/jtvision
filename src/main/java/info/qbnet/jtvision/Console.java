package info.qbnet.jtvision;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;

import java.awt.*;

/**
 * Console class provides a text-mode interface for writing to a screen buffer.
 */
public class Console {

    private final Screen screen;
    private final Backend backend;

    /**
     * Constructs a Console with the given screen buffer and rendering backend.
     * @param screen the screen buffer
     * @param backend the rendering backend
     */
    public Console(Screen screen, Backend backend) {
        this.screen = screen;
        this.backend = backend;
    }

    /**
     * Prints a string at the specified coordinates with specified colors.
     * @param x horizontal position
     * @param y vertical position
     * @param text the text to print
     * @param foreground foreground color
     * @param background background color
     */
    public void putString(int x, int y, String text, Color foreground, Color background) {
        if (text == null) {
            System.err.println("putString(): text is null. Ignored.");
            return;
        }
        if (!screen.isValidColor(foreground, background)) {
            System.err.printf("putString(): null color argument at (%d,%d). Ignored.%n", x, y);
            return;
        }
        if (!screen.isInBounds(x, y)) {
            System.err.printf("putString(): coordinates out of bounds (%d,%d). Ignored.%n", x, y);
            return;
        }

        int maxLength = screen.getWidth() - x;
        int len = Math.min(text.length(), maxLength);
        for (int i = 0; i < len; i++) {
            screen.setChar(x + i, y, text.charAt(i), foreground, background);
        }
        backend.render();
    }

    /**
     * Clears the screen buffer and renders the empty screen.
     */
    public void clearScreen() {
        screen.clear();
        backend.render();
    }

}
