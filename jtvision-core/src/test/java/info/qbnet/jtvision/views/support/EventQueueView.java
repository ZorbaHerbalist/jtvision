package info.qbnet.jtvision.views.support;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TRect;
import java.util.ArrayDeque;

public class EventQueueView extends TestableTView {
    public ArrayDeque<TEvent> events = new ArrayDeque<>();

    public EventQueueView(TRect bounds) {
        super(bounds);
    }

    @Override
    public void getEvent(TEvent event) {
        if (!events.isEmpty()) {
            event.copyFrom(events.removeFirst());
        } else {
            event.what = TEvent.EV_NOTHING;
        }
    }

    @Override
    public void putEvent(TEvent event) {
        events.addFirst(event);
    }
}
