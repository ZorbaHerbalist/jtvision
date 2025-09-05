package info.qbnet.jtvision.views.support;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TGroup;

public class RefusingGroup extends TGroup {
    public RefusingGroup(TRect bounds) {
        super(bounds);
    }

    @Override
    public boolean focus() {
        return false;
    }

    public void clearSelection() {
        current = null;
    }
}
