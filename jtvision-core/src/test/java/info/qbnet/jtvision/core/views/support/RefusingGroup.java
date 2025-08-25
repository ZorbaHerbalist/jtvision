package info.qbnet.jtvision.core.views.support;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TGroup;

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
