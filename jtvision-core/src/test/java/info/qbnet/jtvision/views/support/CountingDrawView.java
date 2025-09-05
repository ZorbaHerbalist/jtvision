package info.qbnet.jtvision.views.support;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TView;

public class CountingDrawView extends TView {
    public int drawCount = 0;

    public CountingDrawView(TRect bounds) {
        super(bounds);
    }

    @Override
    public void draw() {
        drawCount++;
    }
}
