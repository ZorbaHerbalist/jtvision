package info.qbnet.jtvision.views.support;

import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TRect;

public class FocusCountingGroup extends TestGroup {
    public int receivedFocus = 0;

    public FocusCountingGroup(TRect bounds) {
        super(bounds);
    }

    @Override
    public void handleEvent(TEvent event) {
        if (event.what == TEvent.EV_BROADCAST && event.msg.command == Command.CM_RECEIVED_FOCUS) {
            receivedFocus++;
            event.what = TEvent.EV_NOTHING;
        }
        super.handleEvent(event);
    }
}
