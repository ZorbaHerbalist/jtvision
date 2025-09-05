package info.qbnet.jtvision.event;

import info.qbnet.jtvision.event.TEvent;

public class TestFixtures {
    public static TEvent mouseMove(int x, int y) {
        TEvent e = new TEvent();
        e.what = TEvent.EV_MOUSE_MOVE;
        e.mouse.where.x = x;
        e.mouse.where.y = y;
        return e;
    }

    public static TEvent mouseDown(int x, int y) {
        TEvent e = new TEvent();
        e.what = TEvent.EV_MOUSE_DOWN;
        e.mouse.where.x = x;
        e.mouse.where.y = y;
        return e;
    }

    public static TEvent mouseUp(int x, int y) {
        TEvent e = new TEvent();
        e.what = TEvent.EV_MOUSE_UP;
        e.mouse.where.x = x;
        e.mouse.where.y = y;
        return e;
    }

    public static TEvent keyPress(int code) {
        TEvent e = new TEvent();
        e.what = TEvent.EV_KEYDOWN;
        e.key.keyCode = code;
        return e;
    }

    public static TEvent command(int command) {
        TEvent e = new TEvent();
        e.what = TEvent.EV_COMMAND;
        e.msg.command = command;
        return e;
    }
}
