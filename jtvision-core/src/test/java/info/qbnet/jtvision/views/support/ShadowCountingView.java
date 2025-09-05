package info.qbnet.jtvision.views.support;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TView;

public class ShadowCountingView extends TestableTView {
    public int drawUnderViewCalls = 0;

    public ShadowCountingView(TRect bounds) {
        super(bounds);
    }

    @Override
    protected void drawUnderView(boolean doShadow, TView lastView) {
        drawUnderViewCalls++;
    }
}
