package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.objects.TStream;
import info.qbnet.jtvision.core.views.TDrawBuffer;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TInputLine extends TView {

    public static final int CLASS_ID = 8;

    static {
        TStream.registerType(CLASS_ID, TInputLine::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    protected int maxLen;
    protected StringBuilder data;

    protected int firstPos = 0;
    protected int curPos = 0;

    protected int selStart = 0;
    protected int selEnd = 0;

    public static final  TPalette C_INPUT_LINE = new TPalette(TPalette.parseHexString("\\x13\\x13\\x14\\x15"));

    public TInputLine(TRect bounds, int maxLen) {
        super(bounds);
        this.state |= State.SF_CURSOR_VIS;
        this.options |= Options.OF_SELECTABLE + Options.OF_FIRST_CLICK;
        this.maxLen = maxLen;
        this.data = new StringBuilder(maxLen);
    }

    public TInputLine(TStream stream) {
        super(stream);
        try {
            maxLen = stream.readInt();
            String text = stream.readString();
            data = new StringBuilder(maxLen);
            if (text != null) {
                data.append(text);
            }
            firstPos = stream.readInt();
            curPos = stream.readInt();
            selStart = stream.readInt();
            selEnd = stream.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Returns whether the line can be scrolled further by {@code delta}. */
    private boolean canScroll(int delta) {
        if (delta < 0) {
            return firstPos > 0;
        } else if (delta > 0) {
            return data.length() - firstPos + 2 > size.x;
        } else {
            return false;
        }
    }

    @Override
    public int dataSize() {
        return data.toString().getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public void draw() {
        TDrawBuffer buf = new TDrawBuffer();
        short color = (state & State.SF_FOCUSED) != 0 ? getColor((short) 2)
                : getColor((short) 1);

        buf.moveChar(0, ' ', color, size.x);
        int len = Math.min(size.x - 2, data.length() - firstPos);
        if (len > 0) {
            buf.moveStr(1, data.substring(firstPos, firstPos + len), color);
        }
        if (canScroll(1)) {
            buf.moveChar(size.x - 1, (char) 0x10, getColor((short) 4), 1);
        }
        if ((state & State.SF_FOCUSED) != 0) {
            if (canScroll(-1)) {
                buf.moveChar(0, (char) 0x11, getColor((short) 4), 1);
            }
            int l = selStart - firstPos;
            int r = selEnd - firstPos;
            if (l < 0) l = 0;
            if (r > size.x - 2) r = size.x - 2;
            if (l < r) {
                buf.moveChar(l + 1, (char) 0x00, getColor((short) 3), r - l);
            }
        }
        writeLine(0, 0, size.x, size.y, buf.buffer);
    }

    @Override
    public void getData(ByteBuffer dst) {
        byte[] bytes = data.toString().getBytes(StandardCharsets.UTF_8);
        dst.put(bytes);
    }
    @Override
    public TPalette getPalette() {
        return C_INPUT_LINE;
    }

    @Override
    public void setData(ByteBuffer src) {
        byte[] bytes = new byte[src.remaining()];
        src.get(bytes);
        data.setLength(0);
        data.append(new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            stream.writeInt(maxLen);
            stream.writeString(data.toString());
            stream.writeInt(firstPos);
            stream.writeInt(curPos);
            stream.writeInt(selStart);
            stream.writeInt(selEnd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
