package info.qbnet.jtvision.core.views.support;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TView;

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
