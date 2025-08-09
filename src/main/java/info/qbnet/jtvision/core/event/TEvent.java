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
        mouse.where = new TPoint();
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

}
