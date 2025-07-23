package info.qbnet.jtvision.core;

import java.awt.*;

/**
 * Represents a 2D text-based screen buffer.
 */
public class Screen {

    /**
     * Represents a single character cell on the screen.
     */
    public static class ScreenChar {
        char character;
        Color foreground;
        Color background;

        /**
         * Constructs a character cell with specified character and colors.
         * @param character character to display
         * @param foreground foreground color
         * @param background background color
         */
        public ScreenChar(char character, Color foreground, Color background) {
            this.character = character;
            this.foreground = foreground;
            this.background = background;
        }

        public char getCharacter() {
            return character;
        }

        public Color getForeground() {
            return foreground;
        }

        public Color getBackground() {
            return background;
        }
    }

    private final int width;
    private final int height;
    private final ScreenChar[][] buffer;
    private final Color defaultForeground;
    private final Color defaultBackground;
    private final ScreenChar emptyCharTemplate;

    /**
     * Constructs a screen buffer with a given number of columns and rows,
     * and default foreground/background colors.
     * @param width number of columns
     * @param height number of rows
     * @param defaultForeground default foreground color
     * @param defaultBackground default background color
     */
    public Screen(int width, int height, Color defaultForeground, Color defaultBackground) {
        if (width <= 0 || height <= 0) {
            System.err.printf("Invalid screen dimensions: cols=%d, rows=%d. Must be positive.\n", width, height);
            throw new IllegalArgumentException("Screen dimensions must be positive");
        }
        if (defaultForeground == null || defaultBackground == null) {
            System.err.println("Default colors cannot be null.");
            throw new IllegalArgumentException("Default foreground and background colors must not be null");
        }
        this.width = width;
        this.height = height;
        this.defaultForeground = defaultForeground;
        this.defaultBackground = defaultBackground;
        this.emptyCharTemplate = new ScreenChar(' ', defaultForeground, defaultBackground);
        buffer = new ScreenChar[height][width];
        clear();
    }

    /**
     * Constructs a screen buffer with default colors (LIGHT_GRAY on BLACK).
     * @param width number of columns
     * @param height number of rows
     */
    public Screen(int width, int height) {
        this(width, height, Color.LIGHT_GRAY, Color.BLACK);
    }

    /**
     * Sets a character at a specific position.
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @param c character to display
     * @param foreground foreground color
     * @param background background color
     */
    public void setChar(int x, int y, char c, Color foreground, Color background) {
        if (!isInBounds(x, y))
        {
            System.err.printf("setChar(): coordinates out of bounds (%d,%d). Ignored.%n", x, y);
            return;
        }
        if (!isValidColor(foreground, background))
        {
            System.err.printf("setChar(): null color at (%d,%d). Ignored.%n", x, y);
            return;
        }
        buffer[y][x] = new ScreenChar(c, foreground, background);
    }

    /**
     * Sets a character at a specific position with the default color
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @param c character to display
     */
    public void setChar(int x, int y, char c) {
        setChar(x, y, c, defaultForeground, defaultBackground);
    }

    /**
     * Retrieves the character at a given position.
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return character cell at the position or null if out of bounds
     */
    public ScreenChar getChar(int x, int y) {
        if (isInBounds(x, y)) {
            return buffer[y][x];
        }
        System.err.printf("getChar(): coordinates out of bounds (%d,%d). Returning null.%n", x, y);
        return null;
    }

    /**
     * Clears the screen by setting all cells to a space character,
     * with default foreground and background colors.
     */
    public void clear() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[y][x] = emptyCharTemplate;
            }
        }
    }

    /**
     * Checks if coordinates are within screen bounds.
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Checks if foreground and background colors are not null.
     */
    public boolean isValidColor(Color foreground, Color background) {
        return foreground != null && background != null;
    }

    /**
     * @return number of columns
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return number of rows
     */
    public int getHeight() {
        return height;
    }
}
