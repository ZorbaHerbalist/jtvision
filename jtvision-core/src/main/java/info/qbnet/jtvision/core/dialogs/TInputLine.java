package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TInputLine extends TView {

    protected int maxLen;
    protected StringBuilder data;

    public static final  TPalette C_INPUT_LINE = new TPalette(TPalette.parseHexString("\\x13\\x13\\x14\\x15"));

    public TInputLine(TRect bounds, int maxLen) {
        super(bounds);
        this.state |= State.SF_CURSOR_VIS;
        this.options |= Options.OF_SELECTABLE + Options.OF_FIRST_CLICK;
        this.maxLen = maxLen;
        this.data = new StringBuilder(maxLen);
    }

    @Override
    public int dataSize() {
        return data.toString().getBytes(StandardCharsets.UTF_8).length;
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


}
