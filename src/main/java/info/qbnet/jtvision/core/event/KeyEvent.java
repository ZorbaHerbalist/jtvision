package info.qbnet.jtvision.core.event;

public final class KeyEvent implements Event {

    private final int keyCode;      // 0 if using charCode + scanCode
    private final char charCode;    // 0 if using keyCode
    private final int scanCode;     // 0 if using keyCode

    public KeyEvent(int keyCode) {
        this.keyCode = keyCode;
        this.charCode = 0;
        this.scanCode = 0;
    }

    public KeyEvent(char charCode, int scanCode) {
        this.keyCode = 0;
        this.charCode = charCode;
        this.scanCode = scanCode;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public char getCharCode() {
        return charCode;
    }
    public int getScanCode() {
        return scanCode;
    }

    @Override
    public EventType getType() {
        return EventType.EV_KEYDOWN;
    }
}
