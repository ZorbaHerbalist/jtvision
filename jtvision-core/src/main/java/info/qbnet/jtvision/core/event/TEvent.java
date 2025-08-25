package info.qbnet.jtvision.core.event;

import info.qbnet.jtvision.core.objects.TPoint;

public class TEvent {

    /** Event codes. */
    public static final int EV_MOUSE_DOWN   = 0x0001;
    public static final int EV_MOUSE_UP     = 0x0002;
    public static final int EV_MOUSE_MOVE   = 0x0004;
    public static final int EV_MOUSE_AUTO   = 0x0008;
    public static final int EV_KEYDOWN      = 0x0010;
    public static final int EV_COMMAND      = 0x0100;
    public static final int EV_BROADCAST    = 0x0200;

    /** Event masks. */
    public static final int EV_NOTHING      = 0x0000;
    public static final int EV_MOUSE        = 0x000F;
    public static final int EV_KEYBOARD     = 0x0010;
    public static final int EV_MESSAGE      = 0xFF00;

    public static final int POSITIONAL_EVENTS   = EV_MOUSE;
    public static final int FOCUSED_EVENTS      = EV_KEYBOARD + EV_COMMAND;

    /** Identifies the active variant of this event. */
    public int what = EV_NOTHING;

    /** Mouse specific data.  Valid when {@code (what & evMouse) != 0}. */
    public final MouseEvent mouse = new MouseEvent();

    /** Keyboard specific data.  Valid when {@code what == evKeyDown}. */
    public final KeyEvent key = new KeyEvent();

    /** Message data.  Valid when {@code (what & evMessage) != 0}. */
    public final MessageEvent msg = new MessageEvent();

    /** Convenience method mirroring Turbo Vision's ClearEvent procedure. */
    public void clear() {
        what = EV_NOTHING;
        mouse.buttons = 0;
        mouse.isDouble = false;
        mouse.where.x = 0;
        mouse.where.y = 0;
        key.keyCode = 0;
        key.charCode = 0;
        key.scanCode = 0;
        msg.command = 0;
        msg.infoPtr = null;
        msg.infoInt = 0;
        msg.infoLong = 0;
        msg.infoWord = 0;
        msg.infoByte = 0;
        msg.infoChar = 0;
    }

    public void copyFrom(TEvent other) {
        this.what = other.what;
        this.mouse.buttons = other.mouse.buttons;
        this.mouse.isDouble = other.mouse.isDouble;
        this.mouse.where.x = other.mouse.where.x;
        this.mouse.where.y = other.mouse.where.y;
        this.key.keyCode = other.key.keyCode;
        this.key.charCode = other.key.charCode;
        this.key.scanCode = other.key.scanCode;
        this.msg.command = other.msg.command;
        this.msg.infoPtr = other.msg.infoPtr;
        this.msg.infoInt = other.msg.infoInt;
        this.msg.infoLong = other.msg.infoLong;
        this.msg.infoWord = other.msg.infoWord;
        this.msg.infoByte = other.msg.infoByte;
        this.msg.infoChar = other.msg.infoChar;
    }

    /** Data carried by mouse events. */
    public static class MouseEvent {
        public byte buttons;
        public boolean isDouble;
        public TPoint where = new TPoint();
    }

    /** Data carried by key events. */
    public static class KeyEvent {
        /** Full key code (extended) combining scan and character codes. */
        public int keyCode;
        /** ASCII character code of the key, if any. */
        public char charCode;
        /** Scan code part of the key. */
        public byte scanCode;
    }

    /** Data carried by message events. */
    public static class MessageEvent {
        public int command;
        public Object infoPtr;
        public int infoInt;
        public long infoLong;
        public short infoWord;
        public byte infoByte;
        public char infoChar;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(eventName(what));
        if ((what & EV_MOUSE) != 0) {
            sb.append(" buttons=").append(mouse.buttons)
              .append(" where=").append(mouse.where)
              .append(" double=").append(mouse.isDouble);
        } else if (what == EV_KEYDOWN) {
            sb.append(" keyCode=").append(key.keyCode)
              .append(" charCode=").append((int) key.charCode)
              .append(" scanCode=").append(key.scanCode);
        } else if ((what & EV_MESSAGE) != 0) {
            sb.append(" command=").append(msg.command);
        }
        return sb.toString();
    }

    private static String eventName(int what) {
        return switch (what) {
            case EV_MOUSE_DOWN -> "EV_MOUSE_DOWN";
            case EV_MOUSE_UP -> "EV_MOUSE_UP";
            case EV_MOUSE_MOVE -> "EV_MOUSE_MOVE";
            case EV_MOUSE_AUTO -> "EV_MOUSE_AUTO";
            case EV_KEYDOWN -> "EV_KEYDOWN";
            case EV_COMMAND -> "EV_COMMAND";
            case EV_BROADCAST -> "EV_BROADCAST";
            case EV_NOTHING -> "EV_NOTHING";
            default -> "EV_0x" + Integer.toHexString(what);
        };
    }
}
