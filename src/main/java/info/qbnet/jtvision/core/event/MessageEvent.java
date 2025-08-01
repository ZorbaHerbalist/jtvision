package info.qbnet.jtvision.core.event;

public final class MessageEvent implements Event {

    private final int command;
    private final Object info;

    public MessageEvent(int command, int info) {
        this.command = command;
        this.info = info;
    }

    public int getCommand() {
        return command;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInfo() {
        return (T) info;
    }

    @Override
    public EventType getType() {
        return EventType.EV_MESSAGE;
    }
}
