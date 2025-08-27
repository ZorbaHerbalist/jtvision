package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;

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
    public TPalette getPalette() {
        return C_INPUT_LINE;
    }


}
