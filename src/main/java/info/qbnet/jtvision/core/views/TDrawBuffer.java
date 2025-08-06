package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.util.IBuffer;

public class TDrawBuffer {

    public static final int MAX_VIEW_LENGTH = 132;
    private final short[] buffer = new short[MAX_VIEW_LENGTH];

    public void moveChar(int pos, char ch, int attr, int count) {
        int len = Math.min(count, MAX_VIEW_LENGTH - pos);
        short value = (short) ((attr << 8) | (ch & 0xFF));
        for (int i = 0; i < len; i++) {
            buffer[pos + i] = value;
        }
    }

    public void moveStr(int pos, String str, int attr) {
        int len = Math.min(str.length(), MAX_VIEW_LENGTH - pos);
        for (int i = 0; i < len; i++) {
            buffer[pos + i] = (short) ((attr << 8) | (str.charAt(i) & 0xFF));
        }
    }

    public void clear(int attr) {
        moveChar(0, ' ', attr, MAX_VIEW_LENGTH);
    }

//    public void writeLine(IBuffer buffer) {
//        TODO
//    }
}
