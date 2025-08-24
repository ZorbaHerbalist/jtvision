package info.qbnet.jtvision.core.views.support;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TView;

public class DrawCharView extends TView {
    public DrawCharView(TRect bounds) {
        super(bounds);
    }

    @Override
    public void draw() {
        writeChar(0, 0, 'X', 0x07, 1);
    }
}
