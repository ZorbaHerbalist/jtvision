package info.qbnet.jtvision.core.event;

public class KeyEvent implements Event {

    private final int keyCode;
    private final char charCode;
    private final int scanCode;

    public KeyEvent(int keyCode, char charCode, int scanCode) {
        this.keyCode = keyCode;
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


}
