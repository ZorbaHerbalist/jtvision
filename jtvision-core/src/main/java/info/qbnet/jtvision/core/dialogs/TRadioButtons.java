package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.objects.TStream;

import java.nio.ByteBuffer;
import java.util.List;

public class TRadioButtons extends TCluster {

    public static final int CLASS_ID = 14;

    static {
        TStream.registerType(CLASS_ID, TRadioButtons::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    public TRadioButtons(TRect bounds, List<String> strings) {
        super(bounds, strings);
    }

    public TRadioButtons(TStream stream) {
        super(stream);
    }

    @Override
    public void draw() {
        drawMultiBox(" ( ) ", " " + (char) 0x07);
    }

    @Override
    protected boolean mark(int item) {
        return item == value;
    }

    @Override
    protected void movedTo(int item) {
        value = item;
    }

    @Override
    protected void press(int item) {
        value = item;
    }

    @Override
    public void setData(ByteBuffer src) {
        super.setData(src);
        sel = value;
    }
}
