package info.qbnet.jtvision.views.support;

import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TPalette;
import info.qbnet.jtvision.views.TView;

public class TestableTView extends TView {
    private final TPalette palette;

    public TestableTView(TRect bounds) {
        this(bounds, null);
    }

    public TestableTView(TRect bounds, TPalette palette) {
        super(bounds);
        this.palette = palette;
    }

    @Override
    public void draw() {
        // no-op
    }

    @Override
    public TPalette getPalette() {
        return palette;
    }

    public TPoint getOriginField() {
        return origin;
    }

    public TPoint getSizeField() {
        return size;
    }
}
