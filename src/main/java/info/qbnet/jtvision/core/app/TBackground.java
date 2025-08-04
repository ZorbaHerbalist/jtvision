package info.qbnet.jtvision.core.app;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TView;

public class TBackground extends TView {

    private final char pattern;

    public TBackground(TRect bounds, char pattern) {
        super(bounds);
        this.pattern = pattern;
    }

}
