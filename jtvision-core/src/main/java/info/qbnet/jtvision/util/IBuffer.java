package info.qbnet.jtvision.util;

import java.awt.Color;

/**
 * Generic 2D buffer of characters with colour attributes.
 * <p>
 * Coordinates are zero-based and measured from the top-left corner of the
 * buffer. The {@code x} coordinate increases to the right and {@code y}
 * increases downward.
 * </p>
 * <p>
 * Implementations are expected to ignore {@code setChar} calls whose
 * coordinates lie outside of the buffer. Colours must not be {@code null};
 * calls providing {@code null} colours should be ignored. The buffer stores
 * each character together with its Turbo Vision colour attribute packed into a
 * {@code short} value where the low byte contains the character and the high
 * byte the attribute.
 * </p>
 */
public interface IBuffer {

    /**
     * Writes a character using the given foreground and background colours.
     *
     * @param x          zero-based column index
     * @param y          zero-based row index
     * @param c          character to place
     * @param foreground foreground colour, must not be {@code null}
     * @param background background colour, must not be {@code null}
     */
    default void setChar(int x, int y, char c, Color foreground, Color background) {
        if (foreground == null || background == null) return;
        setChar(x, y, c, DosPalette.toAttribute(foreground, background));
    }

    /**
     * Writes a character using a Turbo Vision-style colour attribute byte.
     *
     * <p>The attribute layout is compatible with the DOS palette:
     * bits 0-3 encode the foreground colour, bits 4-6 encode the background,
     * and bit 7 is the background intensity bit.</p>
     *
     * @param x        zero-based column index
     * @param y        zero-based row index
     * @param c        character to place
     * @param attribute colour attribute byte
     */
    void setChar(int x, int y, char c, int attribute);

    /**
     * Reads the packed character/attribute cell at given coordinates.
     *
     * @param x zero-based column index
     * @param y zero-based row index
     * @return packed cell value or {@code 0} if out of range
     */
    short getCell(int x, int y);

    /**
     * Clears the entire buffer, filling it with blank characters using
     * implementation-defined default colours.
     */
    void clear();

    /**
     * @return width of the buffer in columns
     */
    int getWidth();

    /**
     * @return height of the buffer in rows
     */
    int getHeight();

    /**
     * Provides direct access to the underlying buffer storage as a flat
     * array of packed cells in row-major order. Modifications to the
     * returned array affect the contents of the buffer.
     *
     * @return the internal buffer array
     */
    short[] getData();

    /**
     * Registers a listener that is invoked whenever the buffer's
     * contents are modified.
     *
     * @param listener callback executed on buffer changes; may be {@code null}
     */
    void setDirtyListener(Runnable listener);

    /**
     * Retrieves all cells that have changed since the last call and resets
     * their dirty state.
     *
     * <p>The returned list contains the coordinates of cells that were
     * modified by calls to {@link #setChar} or {@link #clear()}. It may be
     * empty if no changes occurred.</p>
     *
     * @return list of dirty cell coordinates
     */
    default java.util.List<TPoint> consumeDirtyCells() {
        return java.util.List.of();
    }

    /**
     * Releases any resources associated with this buffer. Implementations may
     * override to provide cleanup logic; the default implementation does
     * nothing.
     */
    default void dispose() {
    }
}
