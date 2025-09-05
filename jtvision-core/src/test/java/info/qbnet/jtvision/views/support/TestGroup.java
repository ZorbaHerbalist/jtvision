package info.qbnet.jtvision.views.support;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TGroup;
import info.qbnet.jtvision.util.TPalette;

public class TestGroup extends TGroup {
    public boolean resetCurrentCalled = false;
    private final TPalette palette;

    public TestGroup(TRect bounds) {
        this(bounds, null);
    }

    public TestGroup(TRect bounds, TPalette palette) {
        super(bounds);
        this.palette = palette;
    }

    @Override
    protected void resetCurrent() {
        resetCurrentCalled = true;
    }

    @Override
    public TPalette getPalette() {
        return palette;
    }
}
