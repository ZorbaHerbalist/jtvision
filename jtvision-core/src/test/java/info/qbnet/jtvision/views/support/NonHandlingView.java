package info.qbnet.jtvision.views.support;

import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TView;

public class NonHandlingView extends TView {
    public NonHandlingView() {
        super(new TRect(new TPoint(0,0), new TPoint(1,1)));
    }

    @Override
    public void draw() {
        // no-op
    }
}
