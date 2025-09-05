package info.qbnet.jtvision.util;

import java.awt.Color;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Array-based implementation of {@link IBuffer} using a flat array of packed
 * {@code short} values in row-major order. Each cell stores the character in the
 * low byte and the colour attribute in the high byte.
 */
public class Buffer implements IBuffer {

    private final int width;
    private final int height;
    private final short[] buffer;
    private final boolean[] dirty;
    private final Color defaultForeground;
    private final Color defaultBackground;
    private final short emptyCell;
    private Runnable dirtyListener;

    /**
     * Creates a new buffer with the specified dimensions and default colors.
     *
     * @param width            number of columns, must be positive
     * @param height           number of rows, must be positive
     * @param defaultForeground default foreground color
     * @param defaultBackground default background color
     */
    public Buffer(int width, int height, Color defaultForeground, Color defaultBackground) {
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
        int attr = DosPalette.toAttribute(defaultForeground, defaultBackground);
        this.emptyCell = (short) ((attr << 8) | ' ');
        this.buffer = new short[width * height];
        this.dirty = new boolean[width * height];
        clear();
    }

    /**
     * Creates a new buffer with default colors (LIGHT_GRAY on BLACK).
     *
     * @param width  number of columns
     * @param height number of rows
     */
    public Buffer(int width, int height) {
        this(width, height, Color.LIGHT_GRAY, Color.BLACK);
    }

    @Override
    public void setChar(int x, int y, char c, int attribute) {
        if (!isInBounds(x, y)) {
            System.err.printf("setChar(): coordinates out of bounds (%d,%d). Ignored.%n", x, y);
            return;
        }
        int index = y * width + x;
        short newVal = (short) ((attribute << 8) | (c & 0xFF));
        if (buffer[index] == newVal) return;
        buffer[index] = newVal;
        dirty[index] = true;
        if (dirtyListener != null) dirtyListener.run();
    }

    /**
     * Writes a character using the buffer's default colours.
     */
    public void setChar(int x, int y, char c) {
        setChar(x, y, c, DosPalette.toAttribute(defaultForeground, defaultBackground));
    }

    @Override
    public short getCell(int x, int y) {
        if (isInBounds(x, y)) {
            return buffer[y * width + x];
        }
        System.err.printf("getCell(): coordinates out of bounds (%d,%d). Returning 0.%n", x, y);
        return 0;
    }

    @Override
    public void clear() {
        Arrays.fill(buffer, emptyCell);
        Arrays.fill(dirty, true);
        if (dirtyListener != null) dirtyListener.run();
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

    @Override
    public short[] getData() {
        return buffer;
    }

    @Override
    public void setDirtyListener(Runnable listener) {
        this.dirtyListener = listener;
    }

    @Override
    public List<TPoint> consumeDirtyCells() {
        List<TPoint> dirtyCells = new ArrayList<>();
        for (int i = 0; i < dirty.length; i++) {
            if (dirty[i]) {
                dirty[i] = false;
                int x = i % width;
                int y = i / width;
                dirtyCells.add(new TPoint(x, y));
            }
        }
        return dirtyCells;
    }

    @Override
    public void dispose() {
        dirtyListener = null;
    }
}

