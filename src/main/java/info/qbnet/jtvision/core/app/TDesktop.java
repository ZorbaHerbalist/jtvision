package info.qbnet.jtvision.core.app;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TGroup;

public class TDesktop extends TGroup {

    private TBackground background;

    public TDesktop(TRect bounds) {
        super(bounds);
        logger.debug("{} TDesktop()", getLogName());
        if (background != null) {

        }
    }

    public void initBackground() {
        logger.debug("{} initBackground()", getLogName());
        TRect rect = new TRect();
        getExtent(rect);
        background = new TBackground(rect, (char) 176);
    }

}
