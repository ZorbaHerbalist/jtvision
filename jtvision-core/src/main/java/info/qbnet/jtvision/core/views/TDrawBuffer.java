package info.qbnet.jtvision.core.views;

public class TDrawBuffer {

    public static final int MAX_VIEW_LENGTH = 132;
    public final short[] buffer = new short[MAX_VIEW_LENGTH];

    /**
     * Fills the buffer with a repeated character and attribute.
     */
    public void moveChar(int pos, char ch, int attr, int count) {
        int len = Math.min(count, MAX_VIEW_LENGTH - pos);
        short value = (short) ((attr << 8) | (ch & 0xFF));
        for (int i = 0; i < len; i++) {
            buffer[pos + i] = value;
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
