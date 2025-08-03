package info.qbnet.jtvision.util;

import info.qbnet.jtvision.util.buffer.CharacterBuffer;
import info.qbnet.jtvision.util.buffer.CharacterBuffer.CharacterCell;

import java.awt.Color;

/**
 * Array-based implementation of {@link CharacterBuffer} using a 2D array of
 * {@link CharacterCell} objects.
 */
public class ArrayCharacterBuffer implements CharacterBuffer {

    private final int width;
    private final int height;
    private final CharacterCell[][] buffer;
    private final Color defaultForeground;
    private final Color defaultBackground;
    private final CharacterCell emptyCellTemplate;

    /**
     * Creates a new buffer with the specified dimensions and default colors.
     *
     * @param width            number of columns, must be positive
     * @param height           number of rows, must be positive
     * @param defaultForeground default foreground color
     * @param defaultBackground default background color
     */
    public ArrayCharacterBuffer(int width, int height, Color defaultForeground, Color defaultBackground) {
        if (width <= 0 || height <= 0) {
            System.err.printf("Invalid screen dimensions: cols=%d, rows=%d. Must be positive.%n", width, height);
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
        this.emptyCellTemplate = new CharacterCell(' ', defaultForeground, defaultBackground);
        this.buffer = new CharacterCell[height][width];
        clear();
    }

    /**
     * Creates a new buffer with default colors (LIGHT_GRAY on BLACK).
     *
     * @param width  number of columns
     * @param height number of rows
     */
    public ArrayCharacterBuffer(int width, int height) {
        this(width, height, Color.LIGHT_GRAY, Color.BLACK);
    }

    @Override
    public void setChar(int x, int y, char c, Color foreground, Color background) {
        if (!isInBounds(x, y)) {
            System.err.printf("setChar(): coordinates out of bounds (%d,%d). Ignored.%n", x, y);
            return;
        }
        if (!isValidColor(foreground, background)) {
            System.err.printf("setChar(): null color at (%d,%d). Ignored.%n", x, y);
            return;
        }
        buffer[y][x] = new CharacterCell(c, foreground, background);
    }

    /**
     * Writes a character using the buffer's default colors.
     */
    public void setChar(int x, int y, char c) {
        setChar(x, y, c, defaultForeground, defaultBackground);
    }

    @Override
    public CharacterCell getChar(int x, int y) {
        if (isInBounds(x, y)) {
            return buffer[y][x];
        }
        System.err.printf("getChar(): coordinates out of bounds (%d,%d). Returning null.%n", x, y);
        return null;
    }

    @Override
    public void clear() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[y][x] = emptyCellTemplate;
            }
        }
    }

    /**
     * Checks whether the provided coordinates are within the buffer bounds.
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
        }

    /**
     * Validates that the provided colors are non-null.
     */
    public boolean isValidColor(Color foreground, Color background) {
        return foreground != null && background != null;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}

