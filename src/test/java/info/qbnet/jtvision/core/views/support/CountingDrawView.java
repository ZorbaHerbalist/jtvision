package info.qbnet.jtvision.core.views.support;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TView;

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
