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
        BACKGROUND(1, 0x71),
        /** Menu and status line normal text. */
        MENU_NORMAL_TEXT(2, 0x70),
        /** Menu and status line disabled text. */
        MENU_DISABLED_TEXT(3, 0x78),
        /** Menu and status line shortcut text. */
        MENU_SHORTCUT_TEXT(4, 0x74),
        /** Menu and status line normal selection highlight. */
        MENU_NORMAL_SELECTION(5, 0x20),
        /** Menu and status line disabled selection highlight. */
        MENU_DISABLED_SELECTION(6, 0x28),
        /** Menu and status line shortcut selection highlight. */
        MENU_SHORTCUT_SELECTION(7, 0x24),

        /** Blue window passive frame. */
        BLUE_WINDOW_FRAME_PASSIVE(8, 0x17),
        /** Blue window active frame. */
        BLUE_WINDOW_FRAME_ACTIVE(9, 0x1F),
        /** Blue window frame icon. */
        BLUE_WINDOW_FRAME_ICON(10, 0x1A),
        /** Blue window scrollbar page area. */
        BLUE_WINDOW_SCROLLBAR_PAGE(11, 0x31),
        /** Blue window scrollbar controls. */
        BLUE_WINDOW_SCROLLBAR_CONTROLS(12, 0x31),
        /** Blue window scroller normal text. */
        BLUE_WINDOW_SCROLLER_NORMAL(13, 0x1E),
        /** Blue window scroller selected text. */
        BLUE_WINDOW_SCROLLER_SELECTED(14, 0x71),
        /** Reserved blue window slot. */
        BLUE_WINDOW_RESERVED(15, 0x1F),

        /** Cyan window passive frame. */
        CYAN_WINDOW_FRAME_PASSIVE(16, 0x37),
        /** Cyan window active frame. */
        CYAN_WINDOW_FRAME_ACTIVE(17, 0x3F),
        /** Cyan window frame icon. */
        CYAN_WINDOW_FRAME_ICON(18, 0x3A),
        /** Cyan window scrollbar page area. */
        CYAN_WINDOW_SCROLLBAR_PAGE(19, 0x13),
        /** Cyan window scrollbar controls. */
        CYAN_WINDOW_SCROLLBAR_CONTROLS(20, 0x13),
        /** Cyan window scroller normal text. */
        CYAN_WINDOW_SCROLLER_NORMAL(21, 0x3E),
        /** Cyan window scroller selected text. */
        CYAN_WINDOW_SCROLLER_SELECTED(22, 0x21),
        /** Reserved cyan window slot. */
        CYAN_WINDOW_RESERVED(23, 0x3F),

        /** Gray window passive frame. */
        GRAY_WINDOW_FRAME_PASSIVE(24, 0x70),
        /** Gray window active frame. */
        GRAY_WINDOW_FRAME_ACTIVE(25, 0x7F),
        /** Gray window frame icon. */
        GRAY_WINDOW_FRAME_ICON(26, 0x7A),
        /** Gray window scrollbar page area. */
        GRAY_WINDOW_SCROLLBAR_PAGE(27, 0x13),
        /** Gray window scrollbar controls. */
        GRAY_WINDOW_SCROLLBAR_CONTROLS(28, 0x13),
        /** Gray window scroller normal text. */
        GRAY_WINDOW_SCROLLER_NORMAL(29, 0x70),
        /** Gray window scroller selected text. */
        GRAY_WINDOW_SCROLLER_SELECTED(30, 0x7F),
        /** Reserved gray window slot. */
        GRAY_WINDOW_RESERVED(31, 0x7E),

        /** Gray dialog passive frame. */
        GRAY_DIALOG_FRAME_PASSIVE(32, 0x70),
        /** Gray dialog active frame. */
        GRAY_DIALOG_FRAME_ACTIVE(33, 0x7F),
        /** Gray dialog frame icon. */
        GRAY_DIALOG_FRAME_ICON(34, 0x7A),
        /** Gray dialog scrollbar page area. */
        GRAY_DIALOG_SCROLLBAR_PAGE(35, 0x13),
        /** Gray dialog scrollbar controls. */
        GRAY_DIALOG_SCROLLBAR_CONTROLS(36, 0x13),
        /** Gray dialog static text. */
        GRAY_DIALOG_STATIC_TEXT(37, 0x70),
        /** Gray dialog label text. */
        GRAY_DIALOG_LABEL_NORMAL(38, 0x70),
        /** Gray dialog label highlight. */
        GRAY_DIALOG_LABEL_SELECTED(39, 0x7F),
        /** Gray dialog label shortcut. */
        GRAY_DIALOG_LABEL_SHORTCUT(40, 0x7E),
        /** Gray dialog button normal. */
        GRAY_DIALOG_BUTTON_NORMAL(41, 0x20),
        /** Gray dialog default button. */
        GRAY_DIALOG_BUTTON_DEFAULT(42, 0x2B),
        /** Gray dialog pressed button. */
        GRAY_DIALOG_BUTTON_SELECTED(43, 0x2F),
        /** Gray dialog disabled button. */
        GRAY_DIALOG_BUTTON_DISABLED(44, 0x78),
        /** Gray dialog button shortcut. */
        GRAY_DIALOG_BUTTON_SHORTCUT(45, 0x2E),
        /** Gray dialog button shadow. */
        GRAY_DIALOG_BUTTON_SHADOW(46, 0x70),
        /** Gray dialog cluster text. */
        GRAY_DIALOG_CLUSTER_NORMAL(47, 0x30),
        /** Gray dialog cluster selected text. */
        GRAY_DIALOG_CLUSTER_SELECTED(48, 0x3F),
        /** Gray dialog cluster shortcut. */
        GRAY_DIALOG_CLUSTER_SHORTCUT(49, 0x3E),
        /** Gray dialog input line text. */
        GRAY_DIALOG_INPUT_LINE_NORMAL_TEXT(50, 0x1F),
        /** Gray dialog input line selection. */
        GRAY_DIALOG_INPUT_LINE_SELECTED_TEXT(51, 0x2F),
        /** Gray dialog input line arrows. */
        GRAY_DIALOG_INPUT_LINE_ARROWS(52, 0x1A),
        /** Gray dialog history arrow. */
        GRAY_DIALOG_HISTORY_ARROW(53, 0x20),
        /** Gray dialog history sides. */
        GRAY_DIALOG_HISTORY_SIDES(54, 0x72),
        /** Gray dialog history window page area. */
        GRAY_DIALOG_HISTORY_WINDOW_PAGE(55, 0x31),
        /** Gray dialog history window controls. */
        GRAY_DIALOG_HISTORY_WINDOW_CONTROLS(56, 0x31),
        /** Gray dialog list viewer normal item. */
        GRAY_DIALOG_LIST_VIEWER_NORMAL(57, 0x30),
        /** Gray dialog list viewer focused item. */
        GRAY_DIALOG_LIST_VIEWER_FOCUSED(58, 0x2F),
        /** Gray dialog list viewer selected item. */
        GRAY_DIALOG_LIST_VIEWER_SELECTED(59, 0x3E),
        /** Gray dialog list viewer divider. */
        GRAY_DIALOG_LIST_VIEWER_DIVIDER(60, 0x31),
        /** Gray dialog info pane. */
        GRAY_DIALOG_INFO_PANE(61, 0x13),
        /** Gray dialog cluster disabled. */
        GRAY_DIALOG_CLUSTER_DISABLED(62, 0x38),
        /** Gray dialog reserved slot. */
        GRAY_DIALOG_RESERVED(63, 0x00),

        /** Blue dialog passive frame. */
        BLUE_DIALOG_FRAME_PASSIVE(64, 0x17),
        /** Blue dialog active frame. */
        BLUE_DIALOG_FRAME_ACTIVE(65, 0x1F),
        /** Blue dialog frame icon. */
        BLUE_DIALOG_FRAME_ICON(66, 0x1A),
        /** Blue dialog scrollbar page area. */
        BLUE_DIALOG_SCROLLBAR_PAGE(67, 0x71),
        /** Blue dialog scrollbar controls. */
        BLUE_DIALOG_SCROLLBAR_CONTROLS(68, 0x71),
        /** Blue dialog static text. */
        BLUE_DIALOG_STATIC_TEXT(69, 0x1E),
        /** Blue dialog label text. */
        BLUE_DIALOG_LABEL_NORMAL(70, 0x17),
        /** Blue dialog label highlight. */
        BLUE_DIALOG_LABEL_SELECTED(71, 0x1F),
        /** Blue dialog label shortcut. */
        BLUE_DIALOG_LABEL_SHORTCUT(72, 0x1E),
        /** Blue dialog button normal. */
        BLUE_DIALOG_BUTTON_NORMAL(73, 0x20),
        /** Blue dialog default button. */
        BLUE_DIALOG_BUTTON_DEFAULT(74, 0x2B),
        /** Blue dialog pressed button. */
        BLUE_DIALOG_BUTTON_SELECTED(75, 0x2F),
        /** Blue dialog disabled button. */
        BLUE_DIALOG_BUTTON_DISABLED(76, 0x78),
        /** Blue dialog button shortcut. */
        BLUE_DIALOG_BUTTON_SHORTCUT(77, 0x2E),
        /** Blue dialog button shadow. */
        BLUE_DIALOG_BUTTON_SHADOW(78, 0x10),
        /** Blue dialog cluster text. */
        BLUE_DIALOG_CLUSTER_NORMAL(79, 0x30),
        /** Blue dialog cluster selected text. */
        BLUE_DIALOG_CLUSTER_SELECTED(80, 0x3F),
        /** Blue dialog cluster shortcut. */
        BLUE_DIALOG_CLUSTER_SHORTCUT(81, 0x3E),
        /** Blue dialog input line text. */
        BLUE_DIALOG_INPUT_LINE_NORMAL_TEXT(82, 0x70),
        /** Blue dialog input line selection. */
        BLUE_DIALOG_INPUT_LINE_SELECTED_TEXT(83, 0x2F),
        /** Blue dialog input line arrows. */
        BLUE_DIALOG_INPUT_LINE_ARROWS(84, 0x7A),
        /** Blue dialog history arrow. */
        BLUE_DIALOG_HISTORY_ARROW(85, 0x20),
        /** Blue dialog history sides. */
        BLUE_DIALOG_HISTORY_SIDES(86, 0x12),
        /** Blue dialog history window page area. */
        BLUE_DIALOG_HISTORY_WINDOW_PAGE(87, 0x31),
        /** Blue dialog history window controls. */
        BLUE_DIALOG_HISTORY_WINDOW_CONTROLS(88, 0x31),
        /** Blue dialog list viewer normal item. */
        BLUE_DIALOG_LIST_VIEWER_NORMAL(89, 0x30),
        /** Blue dialog list viewer focused item. */
        BLUE_DIALOG_LIST_VIEWER_FOCUSED(90, 0x2F),
        /** Blue dialog list viewer selected item. */
        BLUE_DIALOG_LIST_VIEWER_SELECTED(91, 0x3E),
        /** Blue dialog list viewer divider. */
        BLUE_DIALOG_LIST_VIEWER_DIVIDER(92, 0x31),
        /** Blue dialog info pane. */
        BLUE_DIALOG_INFO_PANE(93, 0x13),
        /** Blue dialog cluster disabled. */
        BLUE_DIALOG_CLUSTER_DISABLED(94, 0x38),
        /** Blue dialog reserved slot. */
        BLUE_DIALOG_RESERVED(95, 0x00),

        /** Cyan dialog passive frame. */
        CYAN_DIALOG_FRAME_PASSIVE(96, 0x37),
        /** Cyan dialog active frame. */
        CYAN_DIALOG_FRAME_ACTIVE(97, 0x3F),
        /** Cyan dialog frame icon. */
        CYAN_DIALOG_FRAME_ICON(98, 0x3A),
        /** Cyan dialog scrollbar page area. */
        CYAN_DIALOG_SCROLLBAR_PAGE(99, 0x13),
        /** Cyan dialog scrollbar controls. */
        CYAN_DIALOG_SCROLLBAR_CONTROLS(100, 0x13),
        /** Cyan dialog static text. */
        CYAN_DIALOG_STATIC_TEXT(101, 0x3E),
        /** Cyan dialog label text. */
        CYAN_DIALOG_LABEL_NORMAL(102, 0x30),
        /** Cyan dialog label highlight. */
        CYAN_DIALOG_LABEL_SELECTED(103, 0x3F),
        /** Cyan dialog label shortcut. */
        CYAN_DIALOG_LABEL_SHORTCUT(104, 0x3E),
        /** Cyan dialog button normal. */
        CYAN_DIALOG_BUTTON_NORMAL(105, 0x20),
        /** Cyan dialog default button. */
        CYAN_DIALOG_BUTTON_DEFAULT(106, 0x2B),
        /** Cyan dialog pressed button. */
        CYAN_DIALOG_BUTTON_SELECTED(107, 0x2F),
        /** Cyan dialog disabled button. */
        CYAN_DIALOG_BUTTON_DISABLED(108, 0x78),
        /** Cyan dialog button shortcut. */
        CYAN_DIALOG_BUTTON_SHORTCUT(109, 0x2E),
        /** Cyan dialog button shadow. */
        CYAN_DIALOG_BUTTON_SHADOW(110, 0x30),
        /** Cyan dialog cluster text. */
        CYAN_DIALOG_CLUSTER_NORMAL(111, 0x70),
        /** Cyan dialog cluster selected text. */
        CYAN_DIALOG_CLUSTER_SELECTED(112, 0x7F),
        /** Cyan dialog cluster shortcut. */
        CYAN_DIALOG_CLUSTER_SHORTCUT(113, 0x7E),
        /** Cyan dialog input line text. */
        CYAN_DIALOG_INPUT_LINE_NORMAL_TEXT(114, 0x1F),
        /** Cyan dialog input line selection. */
        CYAN_DIALOG_INPUT_LINE_SELECTED_TEXT(115, 0x2F),
        /** Cyan dialog input line arrows. */
        CYAN_DIALOG_INPUT_LINE_ARROWS(116, 0x1A),
        /** Cyan dialog history arrow. */
        CYAN_DIALOG_HISTORY_ARROW(117, 0x20),
        /** Cyan dialog history sides. */
        CYAN_DIALOG_HISTORY_SIDES(118, 0x32),
        /** Cyan dialog history window page area. */
        CYAN_DIALOG_HISTORY_WINDOW_PAGE(119, 0x31),
        /** Cyan dialog history window controls. */
        CYAN_DIALOG_HISTORY_WINDOW_CONTROLS(120, 0x71),
        /** Cyan dialog list viewer normal item. */
        CYAN_DIALOG_LIST_VIEWER_NORMAL(121, 0x70),
        /** Cyan dialog list viewer focused item. */
        CYAN_DIALOG_LIST_VIEWER_FOCUSED(122, 0x2F),
        /** Cyan dialog list viewer selected item. */
        CYAN_DIALOG_LIST_VIEWER_SELECTED(123, 0x7E),
        /** Cyan dialog list viewer divider. */
        CYAN_DIALOG_LIST_VIEWER_DIVIDER(124, 0x71),
        /** Cyan dialog info pane. */
        CYAN_DIALOG_INFO_PANE(125, 0x13),
        /** Cyan dialog cluster disabled. */
        CYAN_DIALOG_CLUSTER_DISABLED(126, 0x38),
        /** Cyan dialog reserved slot. */
        CYAN_DIALOG_RESERVED(127, 0x00);

        private final int index;
        private final byte defaultValue;

        ProgramColor(int index, int defaultValue) {
            this.index = index;
            this.defaultValue = PaletteRole.toByte(defaultValue);
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public byte defaultValue() {
            return defaultValue;
        }
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
