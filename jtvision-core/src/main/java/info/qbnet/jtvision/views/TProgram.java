package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.Console;
import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.backend.factory.BackendFactoryProvider;
import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.backend.factory.Factory;
import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TStatusDef;
import info.qbnet.jtvision.util.TStatusItem;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TPalette;
import info.qbnet.jtvision.util.Screen;

import java.util.Optional;

import java.awt.*;

import static info.qbnet.jtvision.util.TPalette.parseHexString;

public class TProgram extends TGroup {

    public static TProgram application = null;
    public static TDesktop desktop = null;
    public static TMenuBar menuBar = null;
    public static TStatusLine statusLine = null;

    private final Screen screen;
    private final Backend backend;
    private final Console console;

    private static int getScreenSize(String key, int defaultValue) {
        String value = System.getProperty(key);
        if (value != null) {
            try {
                int parsed = Integer.parseInt(value);
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException e) {
                System.err.printf("Invalid value for %s: %s. Using default %d.%n", key, value, defaultValue);
            }
        }
        return defaultValue;
    }

    private static final int SCREEN_WIDTH = getScreenSize("console.width", 80);
    private static final int SCREEN_HEIGHT = getScreenSize("console.height", 25);

    public static final TPalette C_APP_COLOR = new TPalette(parseHexString(
            "\\x71\\x70\\x78\\x74\\x20\\x28\\x24\\x17\\x1F\\x1A" +
            "\\x31\\x31\\x1E\\x71\\x1F" +
            "\\x37\\x3F\\x3A\\x13\\x13\\x3E\\x21\\x3F\\x70\\x7F\\x7A\\x13\\x13\\x70\\x7F\\x7E" +
            "\\x70\\x7F\\x7A\\x13\\x13\\x70\\x70\\x7F\\x7E\\x20\\x2B\\x2F\\x78\\x2E\\x70\\x30" +
            "\\x3F\\x3E\\x1F\\x2F\\x1A\\x20\\x72\\x31\\x31\\x30\\x2F\\x3E\\x31\\x13\\x38\\x00" +
            "\\x17\\x1F\\x1A\\x71\\x71\\x1E\\x17\\x1F\\x1E\\x20\\x2B\\x2F\\x78\\x2E\\x10\\x30" +
            "\\x3F\\x3E\\x70\\x2F\\x7A\\x20\\x12\\x31\\x31\\x30\\x2F\\x3E\\x31\\x13\\x38\\x00" +
            "\\x37\\x3F\\x3A\\x13\\x13\\x3E\\x30\\x3F\\x3E\\x20\\x2B\\x2F\\x78\\x2E\\x30\\x70" +
            "\\x7F\\x7E\\x1F\\x2F\\x1A\\x20\\x32\\x31\\x71\\x70\\x2F\\x7E\\x71\\x13\\x38\\x00"
    ));

    private static TEvent pending = new TEvent();

    private static final long DOUBLE_DELAY = 300; // milliseconds
    private static final long REPEAT_DELAY = 100; // milliseconds

    private static int lastMouseButtons = 0;
    private static TPoint lastMousePos = new TPoint();
    private static boolean lastMouseDouble = false;
    private static long lastClickTime = 0;
    private static int lastClickButton = 0;
    private static TPoint lastClickPos = new TPoint();
    private static long lastMouseEventTime = 0;

    /**
     * Creates a new program using the specified backend.
     *
     * @param type the backend type used to render the console
     */
    public TProgram(BackendType type) {
        super(new TRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT));

        logger.debug("{} TProgram@TProgram(type={})", getLogName(), type);

        Factory<? extends Backend> factory = BackendFactoryProvider.getFactory(type);
        factory.initialize();

        this.screen = new Screen(SCREEN_WIDTH, SCREEN_HEIGHT, Color.LIGHT_GRAY, Color.BLACK);
        this.backend = factory.createBackend(screen);
        this.console = new Console(screen, backend);

        application = this;

        this.state = State.SF_VISIBLE | State.SF_SELECTED | State.SF_FOCUSED | State.SF_MODAL | State.SF_EXPOSED;
        this.options = 0;
        this.buffer = screen;

        initDesktop();
        initStatusLine();
        initMenuBar();

        if (desktop != null) {
            insert(desktop);
        }
        if (statusLine != null) {
            insert(statusLine);
        }
        if (menuBar != null) {
            insert(menuBar);
        }
    }

    public static void getKeyEvent(TEvent event) {
        if (application == null) {
            event.what = TEvent.EV_NOTHING;
            return;
        }

        Optional<TEvent> opt = application.backend.pollEvent();
        if (opt.isPresent()) {
            event.copyFrom(opt.get());
        } else {
            event.what = TEvent.EV_NOTHING;
        }
    }

    public static void getMouseEvent(TEvent event) {
        if (application == null) {
            event.what = TEvent.EV_NOTHING;
            return;
        }

        int buttons = application.backend.getMouseButtons();
        TPoint where = application.backend.getMouseLocation();
        long now = System.currentTimeMillis();

        boolean buttonChanged = buttons != lastMouseButtons;
        boolean moved = where.x != lastMousePos.x || where.y != lastMousePos.y;

        event.mouse.buttons = (byte) buttons;
        event.mouse.where = new TPoint(where.x, where.y);

        if (buttonChanged) {
            if ((buttons & ~lastMouseButtons) != 0) {
                int pressed = buttons & ~lastMouseButtons;
                boolean isDouble = pressed == lastClickButton &&
                        (now - lastClickTime) < DOUBLE_DELAY &&
                        where.x == lastClickPos.x && where.y == lastClickPos.y;
                event.what = TEvent.EV_MOUSE_DOWN;
                event.mouse.isDouble = isDouble;
                lastMouseDouble = isDouble;
                lastClickTime = now;
                lastClickButton = pressed;
                lastClickPos = new TPoint(where.x, where.y);
            } else {
                event.what = TEvent.EV_MOUSE_UP;
                event.mouse.isDouble = lastMouseDouble;
            }
            lastMouseEventTime = now;
        } else if (moved) {
            event.what = TEvent.EV_MOUSE_MOVE;
            event.mouse.isDouble = lastMouseDouble;
            lastMouseEventTime = now;
        } else if (buttons != 0 && now - lastMouseEventTime >= REPEAT_DELAY) {
            event.what = TEvent.EV_MOUSE_AUTO;
            event.mouse.isDouble = lastMouseDouble;
            lastMouseEventTime = now;
        } else {
            event.what = TEvent.EV_NOTHING;
        }

        lastMouseButtons = buttons;
        lastMousePos = new TPoint(where.x, where.y);
    }

    @Override
    public void getEvent(TEvent event) {
        if (pending.what != TEvent.EV_NOTHING) {
            event.copyFrom(pending);
            pending.what = TEvent.EV_NOTHING;
        } else {
            getMouseEvent(event);
            if (event.what == TEvent.EV_NOTHING) {
                getKeyEvent(event);
                if (event.what == TEvent.EV_NOTHING) {
                    idle();
                }
            }
        }

        if (event.what == TEvent.EV_KEYDOWN) {
            logger.trace("{} TProgram@getEvent() key=[keyCode={}, charCode={}, scanCode={}]", getLogName(),
                    event.key.keyCode, (int) event.key.charCode, event.key.scanCode);
        }

        if (statusLine != null) {
            boolean keyDown = (event.what & TEvent.EV_KEYDOWN) != 0;
            boolean mouseDown = (event.what & TEvent.EV_MOUSE_DOWN) != 0
                    && firstThat(p -> ((p.state & State.SF_VISIBLE) != 0)
                    && p.mouseInView(event.mouse.where)) == statusLine;
            if (keyDown || mouseDown) {
                statusLine.handleEvent(event);
            }
        }
    }

    @Override
    public TPalette getPalette() {
        return C_APP_COLOR;
    }

    public static byte getShiftState() {
        return (application != null) ? application.backend.getShiftState() : 0;
    }

    /**
     * Provides access to the currently active backend.
     *
     * @return backend instance or {@code null} if program not initialized
     */
    public static Backend getBackend() {
        return (application != null) ? application.backend : null;
    }

    @Override
    public void handleEvent(TEvent event) {
        boolean logEvent = LOG_EVENTS && event.what != TEvent.EV_NOTHING;
        if (logEvent) {
            logger.trace("{} TProgram@handleEvent(event={})", getLogName(), event);
        }
        if (event.what == TEvent.EV_KEYDOWN) {
            char c = KeyCode.getAltChar(event.key.keyCode);
            if (c >= '1' && c <= '9') {
                System.out.println("ALT: " + c);
                if (message(desktop, TEvent.EV_BROADCAST, Command.CM_SELECT_WINDOW_NUM, (byte) c - 0x30) != null) {
                    clearEvent(event);
                }
            }
        }
        super.handleEvent(event);
        if (event.what == TEvent.EV_COMMAND) {
            if (event.msg.command == Command.CM_QUIT) {
                endModal(Command.CM_QUIT);
                clearEvent(event);
            }
        }
        if (logEvent) {
            logger.trace("{} TProgram@handleEvent() eventAfter={} handled={}",
                    getLogName(), event, event.what == TEvent.EV_NOTHING);
        }
    }

    public void idle() {
        if (statusLine != null) {
            statusLine.update();
        }
        if (commandSetChanged) {
            message(this, TEvent.EV_BROADCAST, Command.CM_COMMAND_SET_CHANGED, null);
            commandSetChanged = false;
        }
    }

    public void initDesktop() {
        TRect r = new TRect();
        getExtent(r);
        r.a.y++;
        r.b.y--;
        desktop = new TDesktop(r);
    }

    public void initMenuBar() {
        TRect r = new TRect();
        getExtent(r);
        r.b.y = r.a.y + 1;
        menuBar = new TMenuBar(r, null);
    }

    public static TStatusItem stdStatusKeys(TStatusItem next) {
        return new TStatusItem(null, KeyCode.KB_ALT_X, Command.CM_QUIT,
               new TStatusItem(null, KeyCode.KB_F10, Command.CM_MENU,
               new TStatusItem(null, KeyCode.KB_ALT_F3, Command.CM_CLOSE,
               new TStatusItem(null, KeyCode.KB_F5, Command.CM_ZOOM,
               new TStatusItem(null, KeyCode.KB_CTRL_F5, Command.CM_RESIZE,
               new TStatusItem(null, KeyCode.KB_F6, Command.CM_NEXT,
               new TStatusItem(null, KeyCode.KB_SHIFT_F6, Command.CM_PREV,
               null)))))));
    }

    public void initStatusLine() {
        TRect r = new TRect();
        getExtent(r);
        r.a.y = r.b.y - 1;
        statusLine = new TStatusLine(r,
                new TStatusDef(0, 0xFFFF,
                        new TStatusItem("~Alt-X~ Exit", KeyCode.KB_ALT_X, Command.CM_QUIT,
                        stdStatusKeys(null)),
                null));
    }

    @Override
    public void putEvent(TEvent event) {
        pending.copyFrom(event);
    }

    public void run() {
        try {
            execute();
        } finally {
            console.shutdown();
        }
    }

    // Getters and setters

    /**
     * Returns the console instance created for this program.
     */
    public Console getConsole() {
        logger.trace("{} TProgram@getConsole()", getLogName());

        return console;
    }

}
