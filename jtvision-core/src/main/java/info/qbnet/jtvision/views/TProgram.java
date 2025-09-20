package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.*;
import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.backend.factory.BackendFactoryProvider;
import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.backend.factory.Factory;
import info.qbnet.jtvision.event.TEvent;

import java.util.Optional;

import java.awt.*;

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

    /**
     * Base application palette roles exposed by {@link TProgram}. The indices
     * are grouped into logical sections matching the original Turbo Vision
     * palette layout.
     */
    public enum ProgramColor implements PaletteRole {
        /** Desktop background. */
        BACKGROUND,
        /** Menu and status line normal text. */
        MENU_NORMAL_TEXT,
        /** Menu and status line disabled text. */
        MENU_DISABLED_TEXT,
        /** Menu and status line shortcut text. */
        MENU_SHORTCUT_TEXT,
        /** Menu and status line normal selection highlight. */
        MENU_NORMAL_SELECTION,
        /** Menu and status line disabled selection highlight. */
        MENU_DISABLED_SELECTION,
        /** Menu and status line shortcut selection highlight. */
        MENU_SHORTCUT_SELECTION,

        /** Blue window passive frame. */
        BLUE_WINDOW_FRAME_PASSIVE,
        /** Blue window active frame. */
        BLUE_WINDOW_FRAME_ACTIVE,
        /** Blue window frame icon. */
        BLUE_WINDOW_FRAME_ICON,
        /** Blue window scrollbar page area. */
        BLUE_WINDOW_SCROLLBAR_PAGE,
        /** Blue window scrollbar controls. */
        BLUE_WINDOW_SCROLLBAR_CONTROLS,
        /** Blue window scroller normal text. */
        BLUE_WINDOW_SCROLLER_NORMAL,
        /** Blue window scroller selected text. */
        BLUE_WINDOW_SCROLLER_SELECTED,
        /** Reserved blue window slot. */
        BLUE_WINDOW_RESERVED,

        /** Cyan window passive frame. */
        CYAN_WINDOW_FRAME_PASSIVE,
        /** Cyan window active frame. */
        CYAN_WINDOW_FRAME_ACTIVE,
        /** Cyan window frame icon. */
        CYAN_WINDOW_FRAME_ICON,
        /** Cyan window scrollbar page area. */
        CYAN_WINDOW_SCROLLBAR_PAGE,
        /** Cyan window scrollbar controls. */
        CYAN_WINDOW_SCROLLBAR_CONTROLS,
        /** Cyan window scroller normal text. */
        CYAN_WINDOW_SCROLLER_NORMAL,
        /** Cyan window scroller selected text. */
        CYAN_WINDOW_SCROLLER_SELECTED,
        /** Reserved cyan window slot. */
        CYAN_WINDOW_RESERVED,

        /** Gray window passive frame. */
        GRAY_WINDOW_FRAME_PASSIVE,
        /** Gray window active frame. */
        GRAY_WINDOW_FRAME_ACTIVE,
        /** Gray window frame icon. */
        GRAY_WINDOW_FRAME_ICON,
        /** Gray window scrollbar page area. */
        GRAY_WINDOW_SCROLLBAR_PAGE,
        /** Gray window scrollbar controls. */
        GRAY_WINDOW_SCROLLBAR_CONTROLS,
        /** Gray window scroller normal text. */
        GRAY_WINDOW_SCROLLER_NORMAL,
        /** Gray window scroller selected text. */
        GRAY_WINDOW_SCROLLER_SELECTED,
        /** Reserved gray window slot. */
        GRAY_WINDOW_RESERVED,

        /** Gray dialog passive frame. */
        GRAY_DIALOG_FRAME_PASSIVE,
        /** Gray dialog active frame. */
        GRAY_DIALOG_FRAME_ACTIVE,
        /** Gray dialog frame icon. */
        GRAY_DIALOG_FRAME_ICON,
        /** Gray dialog scrollbar page area. */
        GRAY_DIALOG_SCROLLBAR_PAGE,
        /** Gray dialog scrollbar controls. */
        GRAY_DIALOG_SCROLLBAR_CONTROLS,
        /** Gray dialog static text. */
        GRAY_DIALOG_STATIC_TEXT,
        /** Gray dialog label text. */
        GRAY_DIALOG_LABEL_NORMAL,
        /** Gray dialog label highlight. */
        GRAY_DIALOG_LABEL_SELECTED,
        /** Gray dialog label shortcut. */
        GRAY_DIALOG_LABEL_SHORTCUT,
        /** Gray dialog button normal. */
        GRAY_DIALOG_BUTTON_NORMAL,
        /** Gray dialog default button. */
        GRAY_DIALOG_BUTTON_DEFAULT,
        /** Gray dialog pressed button. */
        GRAY_DIALOG_BUTTON_SELECTED,
        /** Gray dialog disabled button. */
        GRAY_DIALOG_BUTTON_DISABLED,
        /** Gray dialog button shortcut. */
        GRAY_DIALOG_BUTTON_SHORTCUT,
        /** Gray dialog button shadow. */
        GRAY_DIALOG_BUTTON_SHADOW,
        /** Gray dialog cluster text. */
        GRAY_DIALOG_CLUSTER_NORMAL,
        /** Gray dialog cluster selected text. */
        GRAY_DIALOG_CLUSTER_SELECTED,
        /** Gray dialog cluster shortcut. */
        GRAY_DIALOG_CLUSTER_SHORTCUT,
        /** Gray dialog input line text. */
        GRAY_DIALOG_INPUT_LINE_NORMAL_TEXT,
        /** Gray dialog input line selection. */
        GRAY_DIALOG_INPUT_LINE_SELECTED_TEXT,
        /** Gray dialog input line arrows. */
        GRAY_DIALOG_INPUT_LINE_ARROWS,
        /** Gray dialog history arrow. */
        GRAY_DIALOG_HISTORY_ARROW,
        /** Gray dialog history sides. */
        GRAY_DIALOG_HISTORY_SIDES,
        /** Gray dialog history window page area. */
        GRAY_DIALOG_HISTORY_WINDOW_PAGE,
        /** Gray dialog history window controls. */
        GRAY_DIALOG_HISTORY_WINDOW_CONTROLS,
        /** Gray dialog list viewer normal item. */
        GRAY_DIALOG_LIST_VIEWER_NORMAL,
        /** Gray dialog list viewer focused item. */
        GRAY_DIALOG_LIST_VIEWER_FOCUSED,
        /** Gray dialog list viewer selected item. */
        GRAY_DIALOG_LIST_VIEWER_SELECTED,
        /** Gray dialog list viewer divider. */
        GRAY_DIALOG_LIST_VIEWER_DIVIDER,
        /** Gray dialog info pane. */
        GRAY_DIALOG_INFO_PANE,
        /** Gray dialog cluster disabled. */
        GRAY_DIALOG_CLUSTER_DISABLED,
        /** Gray dialog reserved slot. */
        GRAY_DIALOG_RESERVED,

        /** Blue dialog passive frame. */
        BLUE_DIALOG_FRAME_PASSIVE,
        /** Blue dialog active frame. */
        BLUE_DIALOG_FRAME_ACTIVE,
        /** Blue dialog frame icon. */
        BLUE_DIALOG_FRAME_ICON,
        /** Blue dialog scrollbar page area. */
        BLUE_DIALOG_SCROLLBAR_PAGE,
        /** Blue dialog scrollbar controls. */
        BLUE_DIALOG_SCROLLBAR_CONTROLS,
        /** Blue dialog static text. */
        BLUE_DIALOG_STATIC_TEXT,
        /** Blue dialog label text. */
        BLUE_DIALOG_LABEL_NORMAL,
        /** Blue dialog label highlight. */
        BLUE_DIALOG_LABEL_SELECTED,
        /** Blue dialog label shortcut. */
        BLUE_DIALOG_LABEL_SHORTCUT,
        /** Blue dialog button normal. */
        BLUE_DIALOG_BUTTON_NORMAL,
        /** Blue dialog default button. */
        BLUE_DIALOG_BUTTON_DEFAULT,
        /** Blue dialog pressed button. */
        BLUE_DIALOG_BUTTON_SELECTED,
        /** Blue dialog disabled button. */
        BLUE_DIALOG_BUTTON_DISABLED,
        /** Blue dialog button shortcut. */
        BLUE_DIALOG_BUTTON_SHORTCUT,
        /** Blue dialog button shadow. */
        BLUE_DIALOG_BUTTON_SHADOW,
        /** Blue dialog cluster text. */
        BLUE_DIALOG_CLUSTER_NORMAL,
        /** Blue dialog cluster selected text. */
        BLUE_DIALOG_CLUSTER_SELECTED,
        /** Blue dialog cluster shortcut. */
        BLUE_DIALOG_CLUSTER_SHORTCUT,
        /** Blue dialog input line text. */
        BLUE_DIALOG_INPUT_LINE_NORMAL_TEXT,
        /** Blue dialog input line selection. */
        BLUE_DIALOG_INPUT_LINE_SELECTED_TEXT,
        /** Blue dialog input line arrows. */
        BLUE_DIALOG_INPUT_LINE_ARROWS,
        /** Blue dialog history arrow. */
        BLUE_DIALOG_HISTORY_ARROW,
        /** Blue dialog history sides. */
        BLUE_DIALOG_HISTORY_SIDES,
        /** Blue dialog history window page area. */
        BLUE_DIALOG_HISTORY_WINDOW_PAGE,
        /** Blue dialog history window controls. */
        BLUE_DIALOG_HISTORY_WINDOW_CONTROLS,
        /** Blue dialog list viewer normal item. */
        BLUE_DIALOG_LIST_VIEWER_NORMAL,
        /** Blue dialog list viewer focused item. */
        BLUE_DIALOG_LIST_VIEWER_FOCUSED,
        /** Blue dialog list viewer selected item. */
        BLUE_DIALOG_LIST_VIEWER_SELECTED,
        /** Blue dialog list viewer divider. */
        BLUE_DIALOG_LIST_VIEWER_DIVIDER,
        /** Blue dialog info pane. */
        BLUE_DIALOG_INFO_PANE,
        /** Blue dialog cluster disabled. */
        BLUE_DIALOG_CLUSTER_DISABLED,
        /** Blue dialog reserved slot. */
        BLUE_DIALOG_RESERVED,

        /** Cyan dialog passive frame. */
        CYAN_DIALOG_FRAME_PASSIVE,
        /** Cyan dialog active frame. */
        CYAN_DIALOG_FRAME_ACTIVE,
        /** Cyan dialog frame icon. */
        CYAN_DIALOG_FRAME_ICON,
        /** Cyan dialog scrollbar page area. */
        CYAN_DIALOG_SCROLLBAR_PAGE,
        /** Cyan dialog scrollbar controls. */
        CYAN_DIALOG_SCROLLBAR_CONTROLS,
        /** Cyan dialog static text. */
        CYAN_DIALOG_STATIC_TEXT,
        /** Cyan dialog label text. */
        CYAN_DIALOG_LABEL_NORMAL,
        /** Cyan dialog label highlight. */
        CYAN_DIALOG_LABEL_SELECTED,
        /** Cyan dialog label shortcut. */
        CYAN_DIALOG_LABEL_SHORTCUT,
        /** Cyan dialog button normal. */
        CYAN_DIALOG_BUTTON_NORMAL,
        /** Cyan dialog default button. */
        CYAN_DIALOG_BUTTON_DEFAULT,
        /** Cyan dialog pressed button. */
        CYAN_DIALOG_BUTTON_SELECTED,
        /** Cyan dialog disabled button. */
        CYAN_DIALOG_BUTTON_DISABLED,
        /** Cyan dialog button shortcut. */
        CYAN_DIALOG_BUTTON_SHORTCUT,
        /** Cyan dialog button shadow. */
        CYAN_DIALOG_BUTTON_SHADOW,
        /** Cyan dialog cluster text. */
        CYAN_DIALOG_CLUSTER_NORMAL,
        /** Cyan dialog cluster selected text. */
        CYAN_DIALOG_CLUSTER_SELECTED,
        /** Cyan dialog cluster shortcut. */
        CYAN_DIALOG_CLUSTER_SHORTCUT,
        /** Cyan dialog input line text. */
        CYAN_DIALOG_INPUT_LINE_NORMAL_TEXT,
        /** Cyan dialog input line selection. */
        CYAN_DIALOG_INPUT_LINE_SELECTED_TEXT,
        /** Cyan dialog input line arrows. */
        CYAN_DIALOG_INPUT_LINE_ARROWS,
        /** Cyan dialog history arrow. */
        CYAN_DIALOG_HISTORY_ARROW,
        /** Cyan dialog history sides. */
        CYAN_DIALOG_HISTORY_SIDES,
        /** Cyan dialog history window page area. */
        CYAN_DIALOG_HISTORY_WINDOW_PAGE,
        /** Cyan dialog history window controls. */
        CYAN_DIALOG_HISTORY_WINDOW_CONTROLS,
        /** Cyan dialog list viewer normal item. */
        CYAN_DIALOG_LIST_VIEWER_NORMAL,
        /** Cyan dialog list viewer focused item. */
        CYAN_DIALOG_LIST_VIEWER_FOCUSED,
        /** Cyan dialog list viewer selected item. */
        CYAN_DIALOG_LIST_VIEWER_SELECTED,
        /** Cyan dialog list viewer divider. */
        CYAN_DIALOG_LIST_VIEWER_DIVIDER,
        /** Cyan dialog info pane. */
        CYAN_DIALOG_INFO_PANE,
        /** Cyan dialog cluster disabled. */
        CYAN_DIALOG_CLUSTER_DISABLED,
        /** Cyan dialog reserved slot. */
        CYAN_DIALOG_RESERVED;
    }

    public static final PaletteDescriptor<ProgramColor> APP_COLOR_PALETTE =
            PaletteDescriptor.register("program.appColor", ProgramColor.class);

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

    public boolean canMoveFocus() {
        return desktop.valid(Command.CM_RELEASED_FOCUS);
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
        return APP_COLOR_PALETTE.palette();
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
        return TStatusLine.items()
                .item(null, KeyCode.KB_ALT_X, Command.CM_QUIT)
                .item(null, KeyCode.KB_F10, Command.CM_MENU)
                .item(null, KeyCode.KB_ALT_F3, Command.CM_CLOSE)
                .item(null, KeyCode.KB_F5, Command.CM_ZOOM)
                .item(null, KeyCode.KB_CTRL_F5, Command.CM_RESIZE)
                .item(null, KeyCode.KB_F6, Command.CM_NEXT)
                .item(null, KeyCode.KB_SHIFT_F6, Command.CM_PREV)
                .build(next);
    }

    public void initStatusLine() {
        TRect r = new TRect();
        getExtent(r);
        r.a.y = r.b.y - 1;
        TStatusDef defs = TStatusLine.statusLine()
                .def(0, 0xFFFF, b -> b
                        .item("~Alt-X~ Exit", KeyCode.KB_ALT_X, Command.CM_QUIT)
                        .chain(stdStatusKeys(null)))
                .build();
        statusLine = new TStatusLine(r, defs);
    }

    public TWindow insertWindow(TWindow window) {
        if (validView(window) != null) {
            if (canMoveFocus()) {
                desktop.insert(window);
                return window;
            }
        }
        window.done();
        return null;
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

    TView validView(TView v) {
        if (v != null && !v.valid(Command.CM_VALID)) {
            if (!v.valid(Command.CM_VALID)) {
                v.done();
                return null;
            }
        }
        return v;
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
