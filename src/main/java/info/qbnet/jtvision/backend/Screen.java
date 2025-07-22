package info.qbnet.jtvision.backend;

import java.awt.*;

/**
 * Represents a 2D text-based screen buffer.
 */
public class Screen {

    /**
     * Represents a single character cell on the screen.
     */
    public static class ScreenChar {
        char c;
        Color fg;
        Color bg;

        /**
         * Constructs a character cell with specified character and colors.
         * @param c character to display
         * @param fg foreground color
         * @param bg background color
         */
        public ScreenChar(char c, Color fg, Color bg) {
            this.c = c;
            this.fg = fg;
            this.bg = bg;
        }
    }

    private final int cols;
    private final int rows;
    private final ScreenChar[][] buffer;
    private final Color defaultFg;
    private final Color defaultBg;

    /**
     * Constructs a screen buffer with given number of columns and rows,
     * and default foreground/background colors.
     * @param cols number of columns
     * @param rows number of rows
     * @param defaultFg default foreground color
     * @param defaultBg default background color
     */
    public Screen(int cols, int rows, Color defaultFg, Color defaultBg) {
        this.cols = cols;
        this.rows = rows;
        this.defaultFg = defaultFg;
        this.defaultBg = defaultBg;
        buffer = new ScreenChar[rows][cols];
        clear();
    }

    /**
     * Constructs a screen buffer with default colors (LIGHT_GRAY on BLACK).
     * @param cols number of columns
     * @param rows number of rows
     */
    public Screen(int cols, int rows) {
        this(cols, rows, Color.LIGHT_GRAY, Color.BLACK);
    }

    /**
     * Sets a character at a specific position.
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @param c character to display
     * @param fg foreground color
     * @param bg background color
     */
    public void setChar(int x, int y, char c, Color fg, Color bg) {
        if (x >= 0 && x < cols && y >= 0 && y < rows) {
            buffer[y][x] = new ScreenChar(c, fg, bg);
        }
    }

    /**
     * Retrieves the character at a given position.
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @return character cell at the position or null if out of bounds
     */
    public ScreenChar getChar(int x, int y) {
        if (x >= 0 && x < cols && y >= 0 && y < rows) {
            return buffer[y][x];
        }
        return null;
    }

    /**
     * Clears the screen by setting all cells to a space character,
     * with default foreground and background colors.
     */
    public void clear() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                buffer[y][x] = new ScreenChar(' ', defaultFg, defaultBg);
            }
        }
    }

    /**
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }
}
