package info.qbnet.jtvision.core.views.support;

import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TRect;

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
