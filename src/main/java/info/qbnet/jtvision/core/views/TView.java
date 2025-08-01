package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.core.objects.TPoint;
import info.qbnet.jtvision.core.objects.TRect;

public class TView {

    private TPoint origin;
    private TPoint size;

    public TView(TRect bounds) {
        setBounds(bounds);
    }

    private void setBounds(TRect bounds) {
        this.origin = bounds.a;
        this.size = bounds.b;
    }

}
