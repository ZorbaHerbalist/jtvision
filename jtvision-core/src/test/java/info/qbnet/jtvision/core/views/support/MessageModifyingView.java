package info.qbnet.jtvision.core.views.support;

import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TPoint;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TView;

public class MessageModifyingView extends TView {
    public MessageModifyingView() {
        super(new TRect(new TPoint(0,0), new TPoint(1,1)));
    }

    @Override
    public void draw() {
        // no-op
    }

    @Override
    public void handleEvent(TEvent event) {
        if (event.what == TEvent.EV_COMMAND && event.msg.command == Command.CM_OK) {
            event.msg.infoPtr = "modified";
            event.what = TEvent.EV_NOTHING;
        }
        super.handleEvent(event);
    }
}
