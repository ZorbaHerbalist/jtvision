package info.qbnet.jtvision.core.views.support;

import info.qbnet.jtvision.core.objects.TPoint;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TView;

public class NonHandlingView extends TView {
    public NonHandlingView() {
        super(new TRect(new TPoint(0,0), new TPoint(1,1)));
    }

    @Override
    public void draw() {
        // no-op
    }
}
