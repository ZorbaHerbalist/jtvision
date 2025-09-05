package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.objects.TRect;

import java.util.List;

public class TRadioButtons extends TCluster {

    public TRadioButtons(TRect bounds, List<String> strings) {
        super(bounds, strings);
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

}
