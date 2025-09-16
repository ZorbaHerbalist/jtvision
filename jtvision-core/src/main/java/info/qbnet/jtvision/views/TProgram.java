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
        BACKGROUND(1),
        /** Menu and status line normal text. */
        MENU_NORMAL_TEXT(2),
        /** Menu and status line disabled text. */
        MENU_DISABLED_TEXT(3),
        /** Menu and status line shortcut text. */
        MENU_SHORTCUT_TEXT(4),
        /** Menu and status line normal selection highlight. */
        MENU_NORMAL_SELECTION(5),
        /** Menu and status line disabled selection highlight. */
        MENU_DISABLED_SELECTION(6),
        /** Menu and status line shortcut selection highlight. */
        MENU_SHORTCUT_SELECTION(7),

        /** Blue window passive frame. */
        BLUE_WINDOW_FRAME_PASSIVE(8),
        /** Blue window active frame. */
        BLUE_WINDOW_FRAME_ACTIVE(9),
        /** Blue window frame icon. */
        BLUE_WINDOW_FRAME_ICON(10),
        /** Blue window scrollbar page area. */
        BLUE_WINDOW_SCROLLBAR_PAGE(11),
        /** Blue window scrollbar controls. */
        BLUE_WINDOW_SCROLLBAR_CONTROLS(12),
        /** Blue window scroller normal text. */
        BLUE_WINDOW_SCROLLER_NORMAL(13),
        /** Blue window scroller selected text. */
        BLUE_WINDOW_SCROLLER_SELECTED(14),
        /** Reserved blue window slot. */
        BLUE_WINDOW_RESERVED(15),

        /** Cyan window passive frame. */
        CYAN_WINDOW_FRAME_PASSIVE(16),
        /** Cyan window active frame. */
        CYAN_WINDOW_FRAME_ACTIVE(17),
        /** Cyan window frame icon. */
        CYAN_WINDOW_FRAME_ICON(18),
        /** Cyan window scrollbar page area. */
        CYAN_WINDOW_SCROLLBAR_PAGE(19),
        /** Cyan window scrollbar controls. */
        CYAN_WINDOW_SCROLLBAR_CONTROLS(20),
        /** Cyan window scroller normal text. */
        CYAN_WINDOW_SCROLLER_NORMAL(21),
        /** Cyan window scroller selected text. */
        CYAN_WINDOW_SCROLLER_SELECTED(22),
        /** Reserved cyan window slot. */
        CYAN_WINDOW_RESERVED(23),

        /** Gray window passive frame. */
        GRAY_WINDOW_FRAME_PASSIVE(24),
        /** Gray window active frame. */
        GRAY_WINDOW_FRAME_ACTIVE(25),
        /** Gray window frame icon. */
        GRAY_WINDOW_FRAME_ICON(26),
        /** Gray window scrollbar page area. */
        GRAY_WINDOW_SCROLLBAR_PAGE(27),
        /** Gray window scrollbar controls. */
        GRAY_WINDOW_SCROLLBAR_CONTROLS(28),
        /** Gray window scroller normal text. */
        GRAY_WINDOW_SCROLLER_NORMAL(29),
        /** Gray window scroller selected text. */
        GRAY_WINDOW_SCROLLER_SELECTED(30),
        /** Reserved gray window slot. */
        GRAY_WINDOW_RESERVED(31),

        /** Gray dialog passive frame. */
        GRAY_DIALOG_FRAME_PASSIVE(32),
        /** Gray dialog active frame. */
        GRAY_DIALOG_FRAME_ACTIVE(33),
        /** Gray dialog frame icon. */
        GRAY_DIALOG_FRAME_ICON(34),
        /** Gray dialog scrollbar page area. */
        GRAY_DIALOG_SCROLLBAR_PAGE(35),
        /** Gray dialog scrollbar controls. */
        GRAY_DIALOG_SCROLLBAR_CONTROLS(36),
        /** Gray dialog static text. */
        GRAY_DIALOG_STATIC_TEXT(37),
        /** Gray dialog label text. */
        GRAY_DIALOG_LABEL_NORMAL(38),
        /** Gray dialog label highlight. */
        GRAY_DIALOG_LABEL_SELECTED(39),
        /** Gray dialog label shortcut. */
        GRAY_DIALOG_LABEL_SHORTCUT(40),
        /** Gray dialog button normal. */
        GRAY_DIALOG_BUTTON_NORMAL(41),
        /** Gray dialog default button. */
        GRAY_DIALOG_BUTTON_DEFAULT(42),
        /** Gray dialog pressed button. */
        GRAY_DIALOG_BUTTON_SELECTED(43),
        /** Gray dialog disabled button. */
        GRAY_DIALOG_BUTTON_DISABLED(44),
        /** Gray dialog button shortcut. */
        GRAY_DIALOG_BUTTON_SHORTCUT(45),
        /** Gray dialog button shadow. */
        GRAY_DIALOG_BUTTON_SHADOW(46),
        /** Gray dialog cluster text. */
        GRAY_DIALOG_CLUSTER_NORMAL(47),
        /** Gray dialog cluster selected text. */
        GRAY_DIALOG_CLUSTER_SELECTED(48),
        /** Gray dialog cluster shortcut. */
        GRAY_DIALOG_CLUSTER_SHORTCUT(49),
        /** Gray dialog input line text. */
        GRAY_DIALOG_INPUT_LINE_NORMAL_TEXT(50),
        /** Gray dialog input line selection. */
        GRAY_DIALOG_INPUT_LINE_SELECTED_TEXT(51),
        /** Gray dialog input line arrows. */
        GRAY_DIALOG_INPUT_LINE_ARROWS(52),
        /** Gray dialog history arrow. */
        GRAY_DIALOG_HISTORY_ARROW(53),
        /** Gray dialog history sides. */
        GRAY_DIALOG_HISTORY_SIDES(54),
        /** Gray dialog history window page area. */
        GRAY_DIALOG_HISTORY_WINDOW_PAGE(55),
        /** Gray dialog history window controls. */
        GRAY_DIALOG_HISTORY_WINDOW_CONTROLS(56),
        /** Gray dialog list viewer normal item. */
        GRAY_DIALOG_LIST_VIEWER_NORMAL(57),
        /** Gray dialog list viewer focused item. */
        GRAY_DIALOG_LIST_VIEWER_FOCUSED(58),
        /** Gray dialog list viewer selected item. */
        GRAY_DIALOG_LIST_VIEWER_SELECTED(59),
        /** Gray dialog list viewer divider. */
        GRAY_DIALOG_LIST_VIEWER_DIVIDER(60),
        /** Gray dialog info pane. */
        GRAY_DIALOG_INFO_PANE(61),
        /** Gray dialog cluster disabled. */
        GRAY_DIALOG_CLUSTER_DISABLED(62),
        /** Gray dialog reserved slot. */
        GRAY_DIALOG_RESERVED(63),

        /** Blue dialog passive frame. */
        BLUE_DIALOG_FRAME_PASSIVE(64),
        /** Blue dialog active frame. */
        BLUE_DIALOG_FRAME_ACTIVE(65),
        /** Blue dialog frame icon. */
        BLUE_DIALOG_FRAME_ICON(66),
        /** Blue dialog scrollbar page area. */
        BLUE_DIALOG_SCROLLBAR_PAGE(67),
        /** Blue dialog scrollbar controls. */
        BLUE_DIALOG_SCROLLBAR_CONTROLS(68),
        /** Blue dialog static text. */
        BLUE_DIALOG_STATIC_TEXT(69),
        /** Blue dialog label text. */
        BLUE_DIALOG_LABEL_NORMAL(70),
        /** Blue dialog label highlight. */
        BLUE_DIALOG_LABEL_SELECTED(71),
        /** Blue dialog label shortcut. */
        BLUE_DIALOG_LABEL_SHORTCUT(72),
        /** Blue dialog button normal. */
        BLUE_DIALOG_BUTTON_NORMAL(73),
        /** Blue dialog default button. */
        BLUE_DIALOG_BUTTON_DEFAULT(74),
        /** Blue dialog pressed button. */
        BLUE_DIALOG_BUTTON_SELECTED(75),
        /** Blue dialog disabled button. */
        BLUE_DIALOG_BUTTON_DISABLED(76),
        /** Blue dialog button shortcut. */
        BLUE_DIALOG_BUTTON_SHORTCUT(77),
        /** Blue dialog button shadow. */
        BLUE_DIALOG_BUTTON_SHADOW(78),
        /** Blue dialog cluster text. */
        BLUE_DIALOG_CLUSTER_NORMAL(79),
        /** Blue dialog cluster selected text. */
        BLUE_DIALOG_CLUSTER_SELECTED(80),
        /** Blue dialog cluster shortcut. */
        BLUE_DIALOG_CLUSTER_SHORTCUT(81),
        /** Blue dialog input line text. */
        BLUE_DIALOG_INPUT_LINE_NORMAL_TEXT(82),
        /** Blue dialog input line selection. */
        BLUE_DIALOG_INPUT_LINE_SELECTED_TEXT(83),
        /** Blue dialog input line arrows. */
        BLUE_DIALOG_INPUT_LINE_ARROWS(84),
        /** Blue dialog history arrow. */
        BLUE_DIALOG_HISTORY_ARROW(85),
        /** Blue dialog history sides. */
        BLUE_DIALOG_HISTORY_SIDES(86),
        /** Blue dialog history window page area. */
        BLUE_DIALOG_HISTORY_WINDOW_PAGE(87),
        /** Blue dialog history window controls. */
        BLUE_DIALOG_HISTORY_WINDOW_CONTROLS(88),
        /** Blue dialog list viewer normal item. */
        BLUE_DIALOG_LIST_VIEWER_NORMAL(89),
        /** Blue dialog list viewer focused item. */
        BLUE_DIALOG_LIST_VIEWER_FOCUSED(90),
        /** Blue dialog list viewer selected item. */
        BLUE_DIALOG_LIST_VIEWER_SELECTED(91),
        /** Blue dialog list viewer divider. */
        BLUE_DIALOG_LIST_VIEWER_DIVIDER(92),
        /** Blue dialog info pane. */
        BLUE_DIALOG_INFO_PANE(93),
        /** Blue dialog cluster disabled. */
        BLUE_DIALOG_CLUSTER_DISABLED(94),
        /** Blue dialog reserved slot. */
        BLUE_DIALOG_RESERVED(95),

        /** Cyan dialog passive frame. */
        CYAN_DIALOG_FRAME_PASSIVE(96),
        /** Cyan dialog active frame. */
        CYAN_DIALOG_FRAME_ACTIVE(97),
        /** Cyan dialog frame icon. */
        CYAN_DIALOG_FRAME_ICON(98),
        /** Cyan dialog scrollbar page area. */
        CYAN_DIALOG_SCROLLBAR_PAGE(99),
        /** Cyan dialog scrollbar controls. */
        CYAN_DIALOG_SCROLLBAR_CONTROLS(100),
        /** Cyan dialog static text. */
        CYAN_DIALOG_STATIC_TEXT(101),
        /** Cyan dialog label text. */
        CYAN_DIALOG_LABEL_NORMAL(102),
        /** Cyan dialog label highlight. */
        CYAN_DIALOG_LABEL_SELECTED(103),
        /** Cyan dialog label shortcut. */
        CYAN_DIALOG_LABEL_SHORTCUT(104),
        /** Cyan dialog button normal. */
        CYAN_DIALOG_BUTTON_NORMAL(105),
        /** Cyan dialog default button. */
        CYAN_DIALOG_BUTTON_DEFAULT(106),
        /** Cyan dialog pressed button. */
        CYAN_DIALOG_BUTTON_SELECTED(107),
        /** Cyan dialog disabled button. */
        CYAN_DIALOG_BUTTON_DISABLED(108),
        /** Cyan dialog button shortcut. */
        CYAN_DIALOG_BUTTON_SHORTCUT(109),
        /** Cyan dialog button shadow. */
        CYAN_DIALOG_BUTTON_SHADOW(110),
        /** Cyan dialog cluster text. */
        CYAN_DIALOG_CLUSTER_NORMAL(111),
        /** Cyan dialog cluster selected text. */
        CYAN_DIALOG_CLUSTER_SELECTED(112),
        /** Cyan dialog cluster shortcut. */
        CYAN_DIALOG_CLUSTER_SHORTCUT(113),
        /** Cyan dialog input line text. */
        CYAN_DIALOG_INPUT_LINE_NORMAL_TEXT(114),
        /** Cyan dialog input line selection. */
        CYAN_DIALOG_INPUT_LINE_SELECTED_TEXT(115),
        /** Cyan dialog input line arrows. */
        CYAN_DIALOG_INPUT_LINE_ARROWS(116),
        /** Cyan dialog history arrow. */
        CYAN_DIALOG_HISTORY_ARROW(117),
        /** Cyan dialog history sides. */
        CYAN_DIALOG_HISTORY_SIDES(118),
        /** Cyan dialog history window page area. */
        CYAN_DIALOG_HISTORY_WINDOW_PAGE(119),
        /** Cyan dialog history window controls. */
        CYAN_DIALOG_HISTORY_WINDOW_CONTROLS(120),
        /** Cyan dialog list viewer normal item. */
        CYAN_DIALOG_LIST_VIEWER_NORMAL(121),
        /** Cyan dialog list viewer focused item. */
        CYAN_DIALOG_LIST_VIEWER_FOCUSED(122),
        /** Cyan dialog list viewer selected item. */
        CYAN_DIALOG_LIST_VIEWER_SELECTED(123),
        /** Cyan dialog list viewer divider. */
        CYAN_DIALOG_LIST_VIEWER_DIVIDER(124),
        /** Cyan dialog info pane. */
        CYAN_DIALOG_INFO_PANE(125),
        /** Cyan dialog cluster disabled. */
        CYAN_DIALOG_CLUSTER_DISABLED(126),
        /** Cyan dialog reserved slot. */
        CYAN_DIALOG_RESERVED(127);

        private final int index;

        ProgramColor(int index) {
            this.index = index;
        }

        @Override
        public int index() {
            return index;
        }
    }

    private static final String APP_COLOR_DATA =
            "\\x71\\x70\\x78\\x74\\x20\\x28\\x24\\x17\\x1F\\x1A" +
            "\\x31\\x31\\x1E\\x71\\x1F" +
            "\\x37\\x3F\\x3A\\x13\\x13\\x3E\\x21\\x3F\\x70\\x7F\\x7A\\x13\\x13\\x70\\x7F\\x7E" +
            "\\x70\\x7F\\x7A\\x13\\x13\\x70\\x70\\x7F\\x7E\\x20\\x2B\\x2F\\x78\\x2E\\x70\\x30" +
            "\\x3F\\x3E\\x1F\\x2F\\x1A\\x20\\x72\\x31\\x31\\x30\\x2F\\x3E\\x31\\x13\\x38\\x00" +
            "\\x17\\x1F\\x1A\\x71\\x71\\x1E\\x17\\x1F\\x1E\\x20\\x2B\\x2F\\x78\\x2E\\x10\\x30" +
            "\\x3F\\x3E\\x70\\x2F\\x7A\\x20\\x12\\x31\\x31\\x30\\x2F\\x3E\\x31\\x13\\x38\\x00" +
            "\\x37\\x3F\\x3A\\x13\\x13\\x3E\\x30\\x3F\\x3E\\x20\\x2B\\x2F\\x78\\x2E\\x30\\x70" +
            "\\x7F\\x7E\\x1F\\x2F\\x1A\\x20\\x32\\x31\\x71\\x70\\x2F\\x7E\\x71\\x13\\x38\\x00";

    public static final TPalette C_APP_COLOR;

    static {
        PaletteFactory.registerDefaults("program.appColor", ProgramColor.class, APP_COLOR_DATA);
        C_APP_COLOR = PaletteFactory.get("program.appColor");
    }

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
