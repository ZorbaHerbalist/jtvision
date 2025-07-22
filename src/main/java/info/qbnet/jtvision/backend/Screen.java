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

    /**
     * Constructs a screen buffer with given number of columns and rows.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Screen(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        buffer = new ScreenChar[rows][cols];
        clear();
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
     * Clears the screen by setting all cells to a space character.
     */
    public void clear() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                buffer[y][x] = new ScreenChar(' ', Color.LIGHT_GRAY, Color.BLACK);
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
