package info.qbnet.jtvision.core.app;

import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TGroup;

public class TDesktop extends TGroup {

    private TBackground background;

    public TDesktop(TRect bounds) {
        super(bounds);

        logger.debug("{} TDesktop@TDesktop(bounds={})", getLogName(), bounds);

        initBackground();
        if (background != null) {
            insert(background);
        }
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_COMMAND) {
            switch (event.msg.command) {
                case Command.CM_NEXT:
                    focusNext(false);
                    break;
                case Command.CM_PREV:
                    if (valid(Command.CM_RELEASED_FOCUS)) {
                        current.putInFrontOf(background);
                    }
                    break;
                default:
                    return;
            }
            clearEvent(event);
        }
    }

    public void initBackground() {
        logger.trace("{} TDesktop@initBackground()", getLogName());

        TRect rect = new TRect();
        getExtent(rect);
        background = new TBackground(rect, (char) 176);
    }

}
