package info.qbnet.jtvision.core.event;

public sealed interface Event permits NothingEvent, MouseEvent, KeyEvent, MessageEvent {

    EventType getType();

}
