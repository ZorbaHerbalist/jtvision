package info.qbnet.jtvision.util;

import java.awt.Color;

// Palette utilities
import static info.qbnet.jtvision.util.DosPalette.getBackground;
import static info.qbnet.jtvision.util.DosPalette.getForeground;

/**
 * Generic 2D buffer of characters with foreground and background colors.
 * <p>
 * Coordinates are zero-based and measured from the top-left corner of the
 * buffer. The {@code x} coordinate increases to the right and {@code y}
 * increases downward.
 * </p>
 * <p>
 * Implementations are expected to ignore {@code setChar} calls whose
 * coordinates lie outside of the buffer, and {@code getChar} should return
 * {@code null} for such out-of-range locations.
 * Colors must not be {@code null}; calls providing {@code null} colors should
 * be ignored.
 * </p>
 */
public interface IBuffer {

    /**
     * Writes a character with specified colors at given coordinates.
     *
     * @param x          zero-based column index
     * @param y          zero-based row index
     * @param c          character to place
     * @param foreground foreground color, must not be {@code null}
     * @param background background color, must not be {@code null}
     */
    void setChar(int x, int y, char c, Color foreground, Color background);

    /**
     * Writes a character using a Turbo Vision-style color attribute byte.
     *
     * <p>The attribute layout is compatible with the DOS palette:
     * bits 0-3 encode the foreground color, bits 4-6 encode the background,
     * and bit 7 is the background intensity bit.</p>
     *
     * @param x        zero-based column index
     * @param y        zero-based row index
     * @param c        character to place
     * @param attribute color attribute byte
     */
    default void setChar(int x, int y, char c, int attribute) {
        setChar(x, y, c, getForeground(attribute), getBackground(attribute));
    }

    /**
     * Reads the character cell at given coordinates.
     *
     * @param x zero-based column index
     * @param y zero-based row index
     * @return character cell at the coordinates or {@code null} if out of range
     */
    CharacterCell getChar(int x, int y);

    /**
     * Clears the entire buffer, filling it with blank characters using
     * implementation-defined default colors.
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
     * Represents a single character cell within the buffer.
     *
     * @param character  stored character
     * @param foreground foreground color
     * @param background background color
     */
    record CharacterCell(char character, Color foreground, Color background) {
    }
}
