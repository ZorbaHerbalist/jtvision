package info.qbnet.jtvision.util;

public class TDrawBuffer {

    public static final int MAX_VIEW_LENGTH = 132;
    public final short[] buffer = new short[MAX_VIEW_LENGTH];

    /**
     * Fills the buffer with a repeated character and/or attribute.
     * <p>
     * Turbo Vision's {@code MoveChar} treats a zero character or attribute as a
     * signal to preserve the existing value. This behaviour is required when
     * highlighting text – for example {@code TInputLine} selects characters by
     * supplying a null character and a non-zero attribute. The previous
     * implementation always overwrote both fields which caused the highlighted
     * characters to disappear.
     * </p>
     *
     * @param pos  starting position in the buffer
     * @param ch   character to write; {@code 0} preserves the existing one
     * @param attr attribute to write; {@code 0} preserves the existing one
     * @param count number of cells to update
     */
    public void moveChar(int pos, char ch, int attr, int count) {
        int len = Math.min(count, MAX_VIEW_LENGTH - pos);
        for (int i = 0; i < len; i++) {
            int index = pos + i;
            short current = buffer[index];
            int curCh = current & 0xFF;
            int curAttr = (current >>> 8) & 0xFF;

            int newCh = (ch != 0) ? ch & 0xFF : curCh;
            int newAttr = (attr != 0) ? attr & 0xFF : curAttr;

            buffer[index] = (short) ((newAttr << 8) | newCh);
        }
    }

    /**
     * Copies characters from a Java string with one attribute.
     */
    public void moveStr(int pos, String str, int attr) {
        int len = Math.min(str.length(), MAX_VIEW_LENGTH - pos);
        for (int i = 0; i < len; i++) {
            buffer[pos + i] = (short) ((attr << 8) | (str.charAt(i) & 0xFF));
        }
    }

    /**
     * Copies a buffer of characters. If attr is non-zero, the destination
     * receives the supplied attribute; otherwise both character and
     * attribute are copied from the source.
     */
    public void moveBuf(int pos, short[] src, int attr, int count) {
        int len = Math.min(Math.min(count, src.length), MAX_VIEW_LENGTH - pos);
        if (attr != 0) {
            for (int i = 0; i < len; i++) {
                buffer[pos + i] = (short) ((attr << 8) | (src[i] & 0xFF));
            }
        } else {
            for (int i = 0; i < len; i++) {
                buffer[pos + i] = (short) ((buffer[pos + i] & 0xFF00) | (src[i] & 0x00FF));
            }
        }
    }

    /**
     * Copies a string containing ~hot~ key markers. The low byte of attrs
     * holds the normal color, the high byte – the highlighted color.
     */
    public void moveCStr(int pos, String str, int attrs) {
        int normal = attrs & 0xFF;
        int highlight = (attrs >>> 8) & 0xFF;
        boolean hi = false;
        int d = pos;
        for (int i = 0; i < str.length() && d < MAX_VIEW_LENGTH; i++) {
            char ch = str.charAt(i);
            if (ch == '~') {
                hi = !hi;
                continue;
            }
            int attr = hi ? highlight : normal;
            buffer[d++] = (short) ((attr << 8) | (ch & 0xFF));
        }
    }

    /**
     * Clears the entire line with spaces in the given attribute.
     */
    public void clear(int attr) {
        moveChar(0, ' ', attr, MAX_VIEW_LENGTH);
    }

}
