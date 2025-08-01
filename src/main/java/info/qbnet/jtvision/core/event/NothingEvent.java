package info.qbnet.jtvision.core.event;

public final class NothingEvent implements Event {

    @Override
    public EventType getType() {
        return EventType.EV_NOTHING;
    }

}
