package info.qbnet.jtvision.views.support;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TView;

public class DrawCharView extends TView {
    public DrawCharView(TRect bounds) {
        super(bounds);
    }

    @Override
    public void draw() {
        writeChar(0, 0, 'X', 0x07, 1);
    }
}
