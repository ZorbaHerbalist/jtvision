package info.qbnet.jtvision.views;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;
import java.io.IOException;
import info.qbnet.jtvision.util.IBuffer;
import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.TPalette;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Base class for visible UI elements providing geometry, ownership, and linkage.
 * Usually subclassed for widgets such as windows or buttons.
 */
public class TView {

    public static final int CLASS_ID = 1;

    public static void registerType() {
        TStream.registerType(CLASS_ID, TView::new);
    }

    public int getClassId() {
        return CLASS_ID;
    }

    /** Owning {@link TGroup}; {@code null} for top-level views. */
    protected TGroup owner = null;

    /** Next sibling in Z-order; wraps to owner's first child when last. */
    protected TView next = null;

    /** Top-left corner relative to the owner's origin. */
    public TPoint origin;

    /** View dimensions; {@code x} is width and {@code y} is height. */
    public TPoint size;

    /** Cursor position relative to the view's origin. */
    public TPoint cursor = new TPoint(0, 0);

    /** Resize behavior flags for owner size changes. */
    public enum GrowMode {
        /** Keep left edge at constant distance from owner's right. */
        GF_GROW_LO_X,
        /** Keep top edge at constant distance from owner's bottom. */
        GF_GROW_LO_Y,
        /** Keep right edge at constant distance from owner's right. */
        GF_GROW_HI_X,
        /** Keep bottom edge at constant distance from owner's bottom. */
        GF_GROW_HI_Y,
        /** Scale proportionally with owner, e.g. {@code TWindow}. */
        GF_GROW_REL;

        /** Maintain all four edge distances. */
        public static final EnumSet<GrowMode> GF_GROW_ALL =
                EnumSet.of(GF_GROW_LO_X, GF_GROW_LO_Y, GF_GROW_HI_X, GF_GROW_HI_Y);
    }

    /** Current grow mode flags controlling how this view resizes with its owner. */
    private final EnumSet<GrowMode> growMode = EnumSet.noneOf(GrowMode.class);

    private static EnumSet<GrowMode> growModeFromInt(int value) {
        EnumSet<GrowMode> set = EnumSet.noneOf(GrowMode.class);
        if ((value & 0x01) != 0) set.add(GrowMode.GF_GROW_LO_X);
        if ((value & 0x02) != 0) set.add(GrowMode.GF_GROW_LO_Y);
        if ((value & 0x04) != 0) set.add(GrowMode.GF_GROW_HI_X);
        if ((value & 0x08) != 0) set.add(GrowMode.GF_GROW_HI_Y);
        if ((value & 0x10) != 0) set.add(GrowMode.GF_GROW_REL);
        return set;
    }

    private static int growModeToInt(EnumSet<GrowMode> set) {
        int value = 0;
        if (set.contains(GrowMode.GF_GROW_LO_X)) value |= 0x01;
        if (set.contains(GrowMode.GF_GROW_LO_Y)) value |= 0x02;
        if (set.contains(GrowMode.GF_GROW_HI_X)) value |= 0x04;
        if (set.contains(GrowMode.GF_GROW_HI_Y)) value |= 0x08;
        if (set.contains(GrowMode.GF_GROW_REL)) value |= 0x10;
        return value;
    }

    public static class DragMode {
        public static final int DM_DRAG_MOVE = 0x01;
        public static final int DM_DRAG_GROW = 0x02;
        public static final int DM_LIMIT_LO_X = 0x10;
        public static final int DM_LIMIT_LO_Y = 0x20;
        public static final int DM_LIMIT_HI_X = 0x40;
        public static final int DM_LIMIT_HI_Y = 0x80;
        public static final int DM_LIMIT_ALL = DM_LIMIT_LO_X | DM_LIMIT_LO_Y | DM_LIMIT_HI_X | DM_LIMIT_HI_Y;
    }

    public int dragMode = DragMode.DM_LIMIT_LO_Y;

    /** Predefined help context identifiers. */
    public static class HelpContext {
        /** No help topic. */
        public static final int HC_NO_CONTEXT = 0;

        /** Help context during dragging. */
        public static final int HC_DRAGGING = 1;
    }

    protected int helpCtx = HelpContext.HC_NO_CONTEXT;

    /** Runtime state flags; combine {@code SF_*} values with bitwise OR. */
    public static class State {
        /** View is visible. */
        public static final int SF_VISIBLE = 1 << 0;
        /** Cursor is visible. */
        public static final int SF_CURSOR_VIS = 1 << 1;
        /** Cursor is in insert mode. */
        public static final int SF_CURSOR_INS = 1 << 2;
        /** View draws a shadow. */
        public static final int SF_SHADOW = 1 << 3;
        /** View is active. */
        public static final int SF_ACTIVE = 1 << 4;
        /** View is selected. */
        public static final int SF_SELECTED = 1 << 5;
        /** View has focus. */
        public static final int SF_FOCUSED = 1 << 6;
        /** View is being dragged. */
        public static final int SF_DRAGGING = 1 << 7;
        /** View is disabled. */
        public static final int SF_DISABLED = 1 << 8;
        /** View is modal. */
        public static final int SF_MODAL = 1 << 9;
        /** View is the default choice. */
        public static final int SF_DEFAULT = 1 << 10;
        /** View is currently exposed. */
        public static final int SF_EXPOSED = 1 << 11;
    }

    public int state = State.SF_VISIBLE;

    /** Optional behavior flags; combine {@code OF_*} values as needed. */
    public static class Options {
        /** Allow selection. */
        public static final int OF_SELECTABLE = 1 << 0;
        /** Top selection priority. */
        public static final int OF_TOP_SELECT = 1 << 1;
        /** Activate on first click. */
        public static final int OF_FIRST_CLICK = 1 << 2;
        /** Draw a frame. */
        public static final int OF_FRAMED = 1 << 3;
        /** Request pre-processing. */
        public static final int OF_PRE_PROCESS = 1 << 4;
        /** Request post-processing. */
        public static final int OF_POST_PROCESS = 1 << 5;
        /** Use double-buffered drawing. */
        public static final int OF_BUFFERED = 1 << 6;
        /** Allow tiling of the owner. */
        public static final int OF_TILEABLE = 1 << 7;
        /** Center horizontally. */
        public static final int OF_CENTER_X = 1 << 8;
        /** Center vertically. */
        public static final int OF_CENTER_Y = 1 << 9;
        /** Center in both axes. */
        public static final int OF_CENTER = OF_CENTER_X | OF_CENTER_Y;
        /** Validate before changes. */
        public static final int OF_VALIDATE = 1 << 10;
    }

    public int options = 0;

    /** Event classes recognized. {@code 0xFFFF} handles all, {@code 0} handles none. */
    protected int eventMask;

    /* Markers control */
    public static boolean showMarkers = false;

    protected static final int ERROR_ATTR = 0xCF;

    public static final char[] SPECIAL_CHARS =
            {(char)0xAF, (char)0xAE, (char)0x1A, (char)0x1B, ' ', ' '};

    /**
     * Explicit top view pointer; when non-null it overrides the automatic search.
     */
    protected static TView theTopView = null;

    /**
     * Active command set (0–255) excluding standard window commands such as
     * {@link Command#CM_ZOOM} or {@link Command#CM_CLOSE}.
     */
    protected static Set<Integer> curCommandSet = new HashSet<>();

    static {
        for (int i = 0; i < 256; i++) {
            curCommandSet.add(i);
        }
        curCommandSet.remove(Command.CM_ZOOM);
        curCommandSet.remove(Command.CM_CLOSE);
        curCommandSet.remove(Command.CM_RESIZE);
        curCommandSet.remove(Command.CM_NEXT);
        curCommandSet.remove(Command.CM_PREV);
    }

    /**
     * True if the command set has changed since it was last reset.
     */
    protected static boolean commandSetChanged = false;

    /**
     * Enables trace logging in {@link #handleEvent}; override with
     * {@code -Djtvision.logEvents=false}.
     */
    protected static final boolean LOG_EVENTS =
            Boolean.parseBoolean(System.getProperty("jtvision.logEvents", "true"));

    /** Shadow offset from the view. */
    private TPoint shadowSize = new TPoint(2, 1);

    /** Attribute for shadow rendering. */
    private byte shadowAttr = 0x08;

    private static final ConcurrentHashMap<Class<?>, AtomicInteger> CLASS_COUNTERS = new ConcurrentHashMap<>();

    protected final Logger logger;
    private final String logName;

    /**
     * Creates a {@code TView} object with the given {@code bounds} rectangle.
     *
     * @param bounds The rectangle defining the position and size of the view.
     */
    public TView(TRect bounds) {
        logger = LoggerFactory.getLogger(getClass());
        AtomicInteger counter = CLASS_COUNTERS.computeIfAbsent(getClass(), k -> new AtomicInteger());
        logName = getClass().getSimpleName() + "#" + counter.incrementAndGet();

        logger.debug("{} TView@TView(bounds={})", logName, bounds.toString());

        setBounds(bounds);
        eventMask = TEvent.EV_MOUSE_DOWN + TEvent.EV_KEYDOWN + TEvent.EV_COMMAND;
    }

    public TView(TStream stream) {
        logger = LoggerFactory.getLogger(getClass());
        AtomicInteger counter = CLASS_COUNTERS.computeIfAbsent(getClass(), k -> new AtomicInteger());
        logName = getClass().getSimpleName() + "#" + counter.incrementAndGet();

        try {
            origin = new TPoint(stream.readInt(), stream.readInt());
            size = new TPoint(stream.readInt(), stream.readInt());
            cursor = new TPoint(stream.readInt(), stream.readInt());
            growMode.clear();
            growMode.addAll(growModeFromInt(stream.readInt()));
            dragMode = stream.readInt();
            helpCtx = stream.readInt();
            state = stream.readInt();
            options = stream.readInt();
            eventMask = stream.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Hook that is invoked once the view has been fully constructed and inserted
     * into the view hierarchy. Subclasses may override to perform additional
     * initialization that depends on the surrounding views. The default
     * implementation does nothing.
     */
    public void awaken() {
        logger.trace("{} TView@awaken()", logName);
    }

    /** Sets insert-mode cursor (block-style). */
    public void blockCursor() {
        logger.trace("{} TView@blockCursor()", logName);

        setState(State.SF_CURSOR_INS, true);
    }

    /**
     * Computes a new coordinate or size when the owner resizes.
     * Uses {@link GrowMode#GF_GROW_REL} for proportional scaling.
     */
    private int growValue(int value, int s, int d) {
        if (!growMode.contains(GrowMode.GF_GROW_REL))
            return value + d;
        else
            return (value * s + ((s - d) >> 1)) / (s - d);
    }

    /**
     * Computes new bounds after the owner changes size by {@code delta},
     * applying {@code growMode} and clamping via {@link #sizeLimits(TPoint, TPoint)}.
     */
    public void calcBounds(TRect bounds, TPoint delta) {
        getBounds(bounds);

        int sx = owner.size.x;
        int dx = delta.x;
        if (growMode.contains(GrowMode.GF_GROW_LO_X)) {
            bounds.a.x = growValue(bounds.a.x, sx, dx);
        }
        if (growMode.contains(GrowMode.GF_GROW_HI_X)) {
            bounds.b.x = growValue(bounds.b.x, sx, dx);
        }
        if (bounds.b.x - bounds.a.x > TDrawBuffer.MAX_VIEW_LENGTH) {
            bounds.b.x = bounds.a.x + TDrawBuffer.MAX_VIEW_LENGTH;
        }

        int sy = owner.size.y;
        int dy = delta.y;
        if (growMode.contains(GrowMode.GF_GROW_LO_Y)) {
            bounds.a.y = growValue(bounds.a.y, sy, dy);
        }
        if (growMode.contains(GrowMode.GF_GROW_HI_Y)) {
            bounds.b.y = growValue(bounds.b.y, sy, dy);
        }

        TPoint min = new TPoint();
        TPoint max = new TPoint();
        sizeLimits(min, max);
        bounds.b.x = bounds.a.x + range(bounds.b.x - bounds.a.x, min.x, max.x);
        bounds.b.y = bounds.a.y + range(bounds.b.y - bounds.a.y, min.y, max.y);
    }

    /**
     * Updates {@link #origin} and {@link #size} then redraws the view.
     * Internal helper; not for external use.
     */
    public void changeBounds(TRect bounds) {
        logger.trace("{} TView@changeBounds(bounds={})", logName, bounds.toString());
        setBounds(bounds);
        drawView();
    }

    /**
     * Marks {@code event} as handled by setting
     * {@code what} to {@link TEvent#EV_NOTHING} and {@code infoPtr} to {@code this}.
     */
    public void clearEvent(TEvent event) {
        event.what = TEvent.EV_NOTHING;
        event.msg.infoPtr = this;
    }

    /** Checks if {@code command} is enabled in {@link #curCommandSet}. */
    public static boolean commandEnabled(int command) {
        return command > 255 || curCommandSet.contains(command);
    }

    /**
     * Returns the number of bytes required to transfer this view's state.
     * <p>
     * Subclasses overriding this method should return the amount of data that
     * {@link #getData(ByteBuffer)} will write and {@link #setData(ByteBuffer)}
     * will read.
     * </p>
     * The default implementation returns {@code 0}.
     *
     * @return number of bytes representing this view's data
     */
    public int dataSize() {
        return 0;
    }

    /** Removes {@code commands} from {@link #curCommandSet} and flags changes. */
    public static void disableCommands(Set<Integer> commands) {
        if (!Collections.disjoint(curCommandSet, commands)) {
            commandSetChanged = true;
        }
        curCommandSet.removeAll(commands);
    }

    /** Hides this view and asks the owner to delete it. */
    protected void done() {
        logger.trace("{} TView@done()", logName);
        hide();
        if (owner != null) {
            owner.delete(this);
        }
    }

    private void change(TPoint p, TPoint s, int mode, int dx, int dy) {
        if ((mode & DragMode.DM_DRAG_MOVE) != 0 && (TProgram.getShiftState() & 0x03) == 0) {
            p.x += dx;
            p.y += dy;
        } else if ((mode & DragMode.DM_DRAG_GROW) != 0 && (TProgram.getShiftState() & 0x03) != 0) {
            s.x += dx;
            s.y += dy;
        }
    }

    private void update(TPoint p, int mode, int x, int y) {
        if ((mode & DragMode.DM_DRAG_MOVE) != 0) {
            p.x = x;
            p.y = y;
        }
    }

    private void moveGrow(TPoint p, TPoint s, int mode, TRect limits, TPoint minSize, TPoint maxSize) {
        s.x = Math.min(Math.max(s.x, minSize.x), maxSize.x);
        s.y = Math.min(Math.max(s.y, minSize.y), maxSize.y);
        p.x = Math.min(Math.max(p.x, limits.a.x - s.x + 1), limits.b.x - 1);
        p.y = Math.min(Math.max(p.y, limits.a.y - s.y + 1), limits.b.y - 1);
        if ((mode & DragMode.DM_LIMIT_LO_X) != 0) p.x = Math.max(p.x, limits.a.x);
        if ((mode & DragMode.DM_LIMIT_LO_Y) != 0) p.y = Math.max(p.y, limits.a.y);
        if ((mode & DragMode.DM_LIMIT_HI_X) != 0) p.x = Math.min(p.x, limits.b.x - s.x);
        if ((mode & DragMode.DM_LIMIT_HI_Y) != 0) p.y = Math.min(p.y, limits.b.y - s.y);
        TRect r = new TRect(p.x, p.y, p.x + s.x, p.y + s.y);
        locate(r);
    }

    public void dragView(TEvent event, int mode, TRect limits, TPoint minSize, TPoint maxSize) {
        setState(State.SF_DRAGGING, true);
        if (event.what == TEvent.EV_MOUSE_DOWN) {
            if ((mode & DragMode.DM_DRAG_MOVE) != 0) {
                TPoint p = new TPoint(origin.x - event.mouse.where.x, origin.y - event.mouse.where.y);
                do {
                    event.mouse.where.x += p.x;
                    event.mouse.where.y += p.y;
                    moveGrow(event.mouse.where, size, mode, limits, minSize, maxSize);
                } while (mouseEvent(event, TEvent.EV_MOUSE_MOVE));
            } else {
                TPoint p = new TPoint(size.x - event.mouse.where.x, size.y - event.mouse.where.y);
                do {
                    event.mouse.where.x += p.x;
                    event.mouse.where.y += p.y;
                    moveGrow(origin, event.mouse.where, mode, limits, minSize, maxSize);
                } while (mouseEvent(event, TEvent.EV_MOUSE_MOVE));
            }
        } else {
            TRect saveBounds = new TRect();
            getBounds(saveBounds);
            do {
                TPoint p = new TPoint(origin.x, origin.y);
                TPoint s = new TPoint(size.x, size.y);
                keyEvent(event);
                switch (event.key.keyCode) {
                    case KeyCode.KB_SHIFT_LEFT -> change(p, s, mode, -1, 0);
                    case KeyCode.KB_LEFT -> change(p, s, mode, -1, 0);
                    case KeyCode.KB_SHIFT_RIGHT -> change(p, s, mode, 1, 0);
                    case KeyCode.KB_RIGHT -> change(p, s, mode, 1, 0);
                    case KeyCode.KB_SHIFT_UP -> change(p, s, mode, 0, -1);
                    case KeyCode.KB_UP -> change(p, s, mode, 0, -1);
                    case KeyCode.KB_SHIFT_DOWN -> change(p, s, mode, 0, 1);
                    case KeyCode.KB_DOWN -> change(p, s, mode, 0, 1);
                    case KeyCode.KB_SHIFT_CTRL_LEFT -> change(p, s, mode, -8, 0);
                    case KeyCode.KB_CTRL_LEFT -> change(p, s, mode, -8, 0);
                    case KeyCode.KB_SHIFT_CTRL_RIGHT -> change(p, s, mode, 8, 0);
                    case KeyCode.KB_CTRL_RIGHT -> change(p, s, mode, 8, 0);
                    case KeyCode.KB_HOME -> update(p, mode, limits.a.x, p.y);
                    case KeyCode.KB_END -> update(p, mode, limits.b.x - s.x, p.y);
                    case KeyCode.KB_PAGE_UP -> update(p, mode, p.x, limits.a.y);
                    case KeyCode.KB_PAGE_DOWN -> update(p, mode, p.x, limits.b.y - s.y);
                }
                moveGrow(p, s, mode, limits, minSize, maxSize);
            } while (event.key.keyCode != KeyCode.KB_ENTER && event.key.keyCode != KeyCode.KB_ESC);
            if (event.key.keyCode == KeyCode.KB_ESC) {
                locate(saveBounds);
            }
        }
        setState(State.SF_DRAGGING, false);
    }

    /**
     * Draws the entire view. Override in subclasses.
     * Use {@link #drawView()} to redraw only when exposed.
     */
    public void draw() {
        logger.trace("{} TView@draw()", logName);

        TDrawBuffer buf = new TDrawBuffer();
        buf.moveChar(0,' ', getColor((short) 1), size.x);
        writeLine(0,0, size.x, size.y, buf.buffer);
    }

    /**
     * Ensures that the text cursor reflects the current focus state.
     * <p>
     * Whenever the view is focused it delegates to {@link #resetCursor()} which
     * in turn performs the actual cursor update.  When the view does not have
     * the focus, the cursor remains untouched.
     * </p>
     */
    private void drawCursor() {
        if ((state & State.SF_FOCUSED) != 0) {
            resetCursor();
        }
    }

    /**
     * Repaints background (and shadow) after this view is hidden.
     *
     * @param lastView last sibling to repaint up to, or {@code null} for all
     */
    private void drawHide(TView lastView) {
        logger.trace("{} TView@drawHide(lastView={})", logName, lastView != null ? lastView.getLogName() : "null" );

        drawCursor();
        drawUnderView((state & State.SF_SHADOW) != 0, lastView);
    }

    /**
     * Draws the view and optional shadow when it becomes visible.
     *
     * @param lastView last sibling to repaint up to, or {@code null} for all
     */
    private void drawShow(TView lastView) {
        logger.trace("{} TView@drawShow(lastView={})", logName, lastView != null ? lastView.getLogName() : "null" );

        drawView();
        if ((state & State.SF_SHADOW) != 0) {
            drawUnderView(true, lastView);
        }
    }

    /**
     * Repaints siblings beneath {@code rect} up to {@code lastView}.
     */
    private void drawUnderRect(TRect rect, TView lastView) {
        owner.clip.intersect(rect);
        owner.drawSubViews(nextView(), lastView);
        owner.getExtent(owner.clip);
    }

    /**
     * Helper for {@link #drawHide(TView)} and {@link #drawShow(TView)} to repaint
     * obscured siblings with optional shadow.
     */
    protected void drawUnderView(boolean doShadow, TView lastView) {
        logger.trace("{} TView@drawUnderView(doShadow={}, lastView={})", logName, doShadow,
                lastView != null ? lastView.getLogName() : "null");

        TRect r = new TRect();
        getBounds(r);
        if (doShadow) {
            r.b.x += shadowSize.x;
            r.b.y += shadowSize.y;
        }
        drawUnderRect(r, lastView);
    }

    /** Calls {@link #draw()} when {@link #exposed()} is true. */
    public void drawView() {
        logger.trace("{} TView@drawView()", logName);

        if (exposed()) {
            draw();
            drawCursor();
        }
    }

    /**
     * Adds {@code commands} to {@link #curCommandSet} and flags changes.
     */
    public static void enableCommands(Set<Integer> commands) {
        if (!curCommandSet.containsAll(commands)) {
            commandSetChanged = true;
        }
        curCommandSet.addAll(commands);
    }

    /** Terminates the current modal state with {@code command}. */
    public void endModal(int command) {
        TView p = topView();
        if (p != null) {
            p.endModal(command);
        }
    }

    /**
     * Checks whether an event is available without removing it from the queue.
     *
     * @return {@code true} if an event is available; {@code false} otherwise
     */
    public boolean eventAvail() {
        TEvent e = new TEvent();
        getEvent(e);
        if (e.what != TEvent.EV_NOTHING) {
            putEvent(e);
            return true;
        } else {
            return false;
        }
    }

    /** Called when executed modally; default returns {@link Command#CM_CANCEL}. */
    public int execute() {
        return Command.CM_CANCEL;
    }

    /**
     * Internal helper for {@link #exposed()} that determines whether a row
     * segment of {@code target} remains visible after accounting for clipping
     * and for all siblings in front of it.
     *
     * <p>This method mirrors the logic of the original Turbo Vision
     * implementation which recursively analyses the row against every
     * predecessor in Z‑order and then ascends through the owner chain.</p>
     */
    private boolean isRowExposed(TView target, TGroup parent, int y,
                                 int xStart, int xEnd, TView start) {
        if (parent == null) {
            return false;
        }

        // Apply the parent's clipping rectangle.
        TRect clip = parent.clip;
        if (y < clip.a.y || y >= clip.b.y) {
            return false;
        }
        if (xStart < clip.a.x) xStart = clip.a.x;
        if (xEnd > clip.b.x) xEnd = clip.b.x;
        if (xStart >= xEnd) {
            return false;
        }

        TView v = start;
        while (v != null && v != target) {
            if ((v.state & State.SF_VISIBLE) != 0) {
                int vy1 = v.origin.y;
                int vy2 = vy1 + v.size.y;
                if (y >= vy1 && y < vy2) {
                    int vx1 = v.origin.x;
                    int vx2 = vx1 + v.size.x;

                    if (xStart >= vx1) {
                        if (xStart < vx2) {
                            xStart = vx2;
                            if (xStart >= xEnd) return false;
                        }
                    } else if (xEnd > vx1) {
                        if (xEnd <= vx2) {
                            xEnd = vx1;
                            if (xStart >= xEnd) return false;
                        } else {
                            if (isRowExposed(target, parent, y, xStart, vx1, v.nextView())) {
                                return true;
                            }
                            xStart = vx2;
                            if (xStart >= xEnd) return false;
                        }
                    }
                }
            }
            v = v.nextView();
        }

        // No more siblings: ascend to the parent group.
        if (parent.getOwner() == null) {
            return true;
        }

        int newY = y + parent.origin.y;
        int newStart = xStart + parent.origin.x;
        int newEnd = xEnd + parent.origin.x;
        return isRowExposed(parent, parent.getOwner(), newY, newStart,
                newEnd, parent.getOwner().first());
    }

    /**
     * Returns {@code true} if any part of the view is visible on the screen.
     * <p>
     * This includes checking whether the view is marked as visible and whether
     * it is not fully obscured by overlapping sibling views.
     * </p>
     *
     * @return {@code true} if the view is at least partially visible on screen; otherwise {@code false}.
     */
    public boolean exposed() {
        if ((state & State.SF_EXPOSED) == 0) return false;
        if (size.x <= 0 || size.y <= 0) return false;
        if (owner == null) return false;

        for (int y = 0; y < size.y; y++) {
            int rowY = origin.y + y;
            if (isRowExposed(this, owner, rowY, origin.x, origin.x + size.x,
                    owner.first())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to give focus to this view.
     * <p>
     * If the view is neither selected nor modal, it requests focus from its owner. If granted,
     * and if the current focused view either has no validation requirement or validates successfully,
     * this view is selected. Otherwise, focus is denied.
     * </p>
     *
     * @return {@code true} if the focus was successfully set to this view, {@code false} otherwise
     */
    public boolean focus() {
        logger.trace("{} TView@focus()", logName);

        boolean result = true;
        if ((state & (State.SF_SELECTED | State.SF_MODAL)) == 0) {
            if (owner != null) {
                result = owner.focus();
                if (result) {
                    TView current = owner.current;
                    if (current == null ||
                            (current.options & Options.OF_VALIDATE) == 0 ||
                            current.valid(Command.CM_RELEASED_FOCUS)) {
                        select();
                    } else {
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns, in the {@code bounds} parameter, the bounding rectangle of the view
     * in its owner's coordinate system.
     * <p>
     * {@code bounds.a} is set to {@code origin}, and {@code bounds.b} is set to the sum
     * of {@code origin} and {@code size}.
     * </p>
     *
     * @param bounds The {@link TRect} object to populate with the view's bounds.
     */
    void getBounds(TRect bounds) {
        bounds.a = new TPoint(origin.x, origin.y);
        bounds.b = new TPoint(origin.x + size.x, origin.y + size.y);
    }

    /**
     * Returns, in {@code clip}, the minimum rectangle that needs redrawing during a
     * call to {@code draw()}. For complicated views, this method can help improve
     * performance noticeably by avoiding unnecessary redraws.
     * <p>
     * The method returns the intersection of the view’s bounding rectangle and its owner's
     * clip rectangle. It adjusts the coordinates so that the result is in the view's
     * local space (i.e., relative to the view’s origin).
     * </p>
     *
     * @param clip The {@link TRect} to populate with the clip rectangle.
     */
    public void getClipRect(TRect clip) {
        getBounds(clip);
        if (owner != null) {
            clip.intersect(owner.clip);
        }
        clip.move(-origin.x, -origin.y);
    }

    /**
     * Maps the palette indexes in the low and high bytes of {@code color} into physical
     * character attributes by tracing through the palette of the view and the palettes
     * of all its owners.
     * <p>
     * This is a public wrapper around {@code mapCPair()}.
     * </p>
     *
     * @param color color pair encoded in a 16-bit word
     * @return mapped attribute pair
     */
    public short getColor(short color) {
        return mapCPair(color);
    }

    /**
     * Returns a copy of the current command set for this view.
     * <p>
     * The returned set contains all currently enabled command identifiers. Modifying the returned
     * set will not affect the view's actual command set.
     * </p>
     *
     * @return a new {@link Set} containing the enabled commands
     */
    public static Set<Integer> getCommands() {
        return new HashSet<>(curCommandSet);
    }

    /**
     * Copies this view's state into the provided destination buffer.
     * <p>
     * The buffer must have at least {@link #dataSize()} bytes remaining. The
     * default implementation does nothing.
     * </p>
     *
     * @param dst destination buffer
     */
    public void getData(ByteBuffer dst) {}

    /**
     * Returns the next available event in the given {@link TEvent} argument.
     * <p>
     * If no event is available, this method should set the event to {@code TEvent.EV_NOTHING}.
     * By default, it calls the owner's {@code getEvent} method to retrieve the event.
     * </p>
     *
     * @param event the {@link TEvent} object that will receive the next available event
     */
    public void getEvent(TEvent event) {
        if (owner != null) {
            owner.getEvent(event);
        }
    }

    /** Fills {@code extent} with the view's local bounds. */
    public void getExtent(TRect extent) {
        extent.a = new TPoint(0, 0);
        extent.b = new TPoint(size.x, size.y);
    }

    /** Returns {@link #helpCtx} or {@link HelpContext#HC_DRAGGING} when dragging. */
    public int getHelpCtx() {
        if ((state & State.SF_DRAGGING) != 0) {
            return HelpContext.HC_DRAGGING;
        } else {
            return helpCtx;
        }
    }

    /**
     * @return this view's palette or {@code null} to disable color translation.
     * Subclasses usually override.
     */
    public TPalette getPalette() {
        return null;
    }

    /**
     * Checks whether all flags in the given mask are set in this view's state.
     *
     * @param mask the bitmask of state flags to test
     * @return {@code true} if all specified flags are set; {@code false} otherwise
     */
    public boolean getState(int mask) {
        return (state & mask) == mask;
    }

    public void growTo(int x, int y) {
        TRect r = new TRect(origin.x, origin.y, origin.x + x, origin.y + y);
        locate(r);
    }

    /**
     * Dispatches events. Default handles {@link TEvent#EV_MOUSE_DOWN} by
     * selecting the view when appropriate. Use {@link #clearEvent(TEvent)} to
     * mark events as handled.
     */
    public void handleEvent(TEvent event) {
        boolean logEvent = LOG_EVENTS && event.what != TEvent.EV_NOTHING;
        if (logEvent) {
            logger.trace("{} TView@handleEvent(event={})", logName, event);
        }

        if (event.what == TEvent.EV_MOUSE_DOWN) {
            if ((state & (State.SF_SELECTED + State.SF_DISABLED)) == 0 && (options & Options.OF_SELECTABLE) != 0) {
                if (!focus() || (options & Options.OF_FIRST_CLICK) == 0) {
                    clearEvent(event);
                }
            }
        }

        if (logEvent) {
            logger.trace("{} TView@handleEvent() eventAfter={} handled={}",
                    logName, event, event.what == TEvent.EV_NOTHING);
        }
    }

    /**
     * Hides the view by calling {@link #setState} to clear the {@code SF_VISIBLE} flag in {@code state}.
     */
    public void hide() {
        logger.trace("{} TView@hide()", logName);

        if ((state & State.SF_VISIBLE) != 0) {
            setState(State.SF_VISIBLE, false);
        }
    }

    /** Hides the text cursor. */
    public void hideCursor() {
        logger.trace("{} TView@hideCursor()", logName);

        setState(State.SF_CURSOR_VIS, false);
    }

    /** Returns the next keydown event. */
    public void keyEvent(TEvent event) {
        do {
            getEvent(event);
        } while (event.what != TEvent.EV_KEYDOWN);
    }

    /**
     * Ensures that {@code value} falls within the inclusive range defined by
     * {@code min} and {@code max}.
     *
     * @param value the value to be checked
     * @param min   the minimum permitted value
     * @param max   the maximum permitted value
     * @return {@code min} if {@code value} is below the range, {@code max} if above,
     *         otherwise {@code value} unchanged
     */
    private int range(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Changes the bounds of the view to the specified {@code bounds} and redraws the view at its new location.
     * <p>
     * This method first calls {@code sizeLimits} to ensure that the given {@code bounds} are valid.
     * If the new bounds differ from the current bounds, it updates the view's position and size by calling
     * {@code changeBounds}. If the view is visible, it also redraws the underlying area (including shadows if present).
     * </p>
     * <p>
     * Validates bounds using {@code sizeLimits}, updates them via {@code changeBounds}, and redraws the view.
     * </p>
     *
     * @param bounds the target rectangle bounds for the view
     */
    public void locate(TRect bounds) {
        TPoint min = new TPoint();
        TPoint max = new TPoint();
        sizeLimits(min, max);
        bounds.b.x = bounds.a.x + range(bounds.b.x - bounds.a.x, min.x, max.x);
        bounds.b.y = bounds.a.y + range(bounds.b.y - bounds.a.y, min.y, max.y);
        TRect r = new TRect();
        getBounds(r);
        if (!bounds.equals(r)) {
            changeBounds(bounds);
            if (owner != null && (state & State.SF_VISIBLE) != 0) {
                if ((state & State.SF_SHADOW) != 0) {
                    r.union(bounds);
                    r.b.x += shadowSize.x;
                    r.b.y += shadowSize.y;
                }
                drawUnderRect(r, null);
            }
        }
    }

    /** Moves this view to the top of its owner's subview list. */
    public void makeFirst() {
        putInFrontOf(owner.first());
    }

    /**
     * Converts {@code source} from local coordinates to global and stores in {@code dest}.
     */
    public void makeGlobal(TPoint source, TPoint dest) {
        TView current = this;
        dest.x = source.x;
        dest.y = source.y;
        do {
            dest.x += current.origin.x;
            dest.y += current.origin.y;
            current = current.owner;
        } while (current != null);
    }
    /**
     * Converts {@code source} from global to local coordinates into {@code dest}.
     */
    public void makeLocal(TPoint source, TPoint dest) {
        TView current = this;
        dest.x = source.x;
        dest.y = source.y;
        do {
            dest.x -= current.origin.x;
            dest.y -= current.origin.y;
            current = current.owner;
        } while (current != null);
    }

    /** Translates a palette index to an attribute using the palette chain. */
    private int mapColor(int color) {
        if (color == 0) return ERROR_ATTR;

        TView view = this;
        while (view != null) {
            TPalette palette = view.getPalette();
            if (palette != null) {
                if (color > palette.length()) return ERROR_ATTR;
                color = palette.get(color);
                if (color == 0) return ERROR_ATTR;
            }
            view = view.owner;
        }

        return color;
    }

    /** Converts a packed foreground/background pair into an attribute. */
    private short mapCPair(short colorPair) {
        int background = (colorPair >> 8) & 0xff;
        int foreground = colorPair & 0xff;
        if (background != 0) {
            background = mapColor(background);
        }
        foreground = mapColor(foreground);
        return (short) (background << 8 | foreground & 0xff);
    }

    /** Returns the next mouse event matching {@code mask} or a button release. */
    public boolean mouseEvent(TEvent event, int mask) {
        do {
            getEvent(event);
        } while ((event.what & (mask | TEvent.EV_MOUSE_UP)) == 0);
        return event.what != TEvent.EV_MOUSE_UP;
    }

    /** Checks whether global {@code mouse} coordinates fall inside this view. */
    public boolean mouseInView(TPoint mouse) {
        TRect extent = new TRect();
        TPoint local = new TPoint(mouse.x, mouse.y);
        makeLocal(mouse, local);
        getExtent(extent);
        return extent.contains(local);
    }

    public void moveTo(int x, int y) {
        TRect r = new TRect(x, y, x + size.x, y + size.y);
        locate(r);
    }

    /** @return next subview or {@code null} if this is the last. */
    public TView nextView() {
        if (owner.last == this) {
            return null;
        } else {
            return next;
        }
    }

    /** Clears insert mode (show underline cursor). */
    public void normalCursor() {
        logger.trace("{} TView@normalCursor()", logName);

        setState(State.SF_CURSOR_INS, false);
    }

    /** @return previous subview, treating the list as circular. */
    public TView prev() {
        TView previous = this;
        TView np = next;

        while ((np != null) && (np != this)) {
            previous = np;
            np = np.next;
        }

        return previous;
    }

    /** @return previous subview or {@code null} if this is the first. */
    public TView prevView() {
        return owner != null && owner.first() != this ? prev() : null;
    }

    /** Forwards {@code event} to the owner's event queue. */
    public void putEvent(TEvent event) {
        if (owner != null) {
            owner.putEvent(event);
        }
    }

    /**
     * Relocates this view within its owner's subview list relative to a
     * specified {@code target} view.
     * <p>
     * The method removes the view from its current position and reinserts it
     * before {@code target}, effectively reordering the owner's child views.
     * Used internally when changing z-order.
     * </p>
     *
     * @param target the view before which this view should be inserted
     */
    private void moveView(TView target) {
        owner.removeView(this);
        owner.insertView(this, target);
    }

    /**
     * Moves the calling view in front of the specified {@code target} in the owner's subview list.
     * <p>
     * The call {@code putInFrontOf(owner.first())} is equivalent to {@link #makeFirst()}.
     * This method works by changing pointers in the subview list. Depending on the
     * position of other views and their visibility states, {@code putInFrontOf} may cause
     * this view to obscure (clip) underlying views. If the view is selectable
     * ({@link Options#OF_SELECTABLE}) and is moved in front of all other subviews,
     * it becomes the selected view.
     * </p>
     *
     * @param target the view in front of which this view should be placed.
     */
    public void putInFrontOf(TView target) {
        if (owner != null && target != this && target != nextView() && (target == null || target.owner == owner)) {
            if ((state & State.SF_VISIBLE) == 0) {
                moveView(target);
            } else {
                TView lastView = nextView();
                if (lastView != null) {
                    TView p = target;
                    while (p != null && p != lastView) {
                        p = p.nextView();
                    }
                    if (p == null) {
                        lastView = target;
                    }
                }
                state &= ~State.SF_VISIBLE;
                if (lastView == target) {
                    drawHide(lastView);
                }
                moveView(target);
                state |= State.SF_VISIBLE;
                if (lastView != target) {
                    drawShow(lastView);
                }
                if ((options & Options.OF_SELECTABLE) != 0) {
                    owner.resetCurrent();
                    owner.resetCursor();
                }
            }
        }
    }

    /**
     * Resets the hardware cursor to match this view's cursor position.
     *
     * <p>This Java translation currently performs only state checks and
     * computes the global cursor location. Integration with a real backend
     * should position the terminal cursor accordingly.</p>
     */
    protected void resetCursor() {
        // Determine if cursor should be shown
        int required = State.SF_VISIBLE | State.SF_CURSOR_VIS | State.SF_FOCUSED;
        boolean show = (state & required) == required;

        TPoint global = new TPoint(cursor.x, cursor.y);
        if (show) {
            if (cursor.x < 0 || cursor.x >= size.x || cursor.y < 0 || cursor.y >= size.y) {
                show = false; // outside bounds
            } else {
                makeGlobal(global, global);
            }
        }

        Backend backend = TProgram.getBackend();
        if (backend != null) {
            backend.updateCursor(global.x, global.y,
                    (state & State.SF_CURSOR_INS) != 0, show);
        }
        logger.trace("{} TView@resetCursor() global=({}, {}) show={}",
                logName, global.x, global.y, show);
    }

    /**
     * Selects this view (sets {@link State#SF_SELECTED} flag). If the view's owner is focused,
     * the view also becomes focused ({@link State#SF_FOCUSED}).
     * <p>
     * If the {@link Options#OF_TOP_SELECT} flag is set in this view's {@code options}, the view is moved
     * to the top of its owner's subview list via {@link #makeFirst()}. Otherwise, the owner's
     * current view is set to this view using {@link TGroup #setCurrent(TView, int)}.
     * </p>
     */
    public void select() {
        logger.trace("{} TView@select()", logName);
        if ((options & Options.OF_SELECTABLE) != 0) {
            if ((options & Options.OF_TOP_SELECT) != 0) {
                makeFirst();
            } else {
                if (owner != null) {
                    owner.setCurrent(this, TGroup.SelectMode.NORMAL_SELECT);
                }
            }
        }
    }

    /**
     * Sets the bounding rectangle of the view to the value given by the {@code bounds} parameter.
     * <p>
     * The {@code origin} field is set to {@code bounds.a}, and the {@code size} field is set
     * to the difference between {@code bounds.b} and {@code bounds.a}.
     * </p>
     * <p>
     * This method is intended to be called only from within an overridden {@code changeBounds}
     * method. You should never call {@code setBounds} directly.
     * </p>
     *
     * @param bounds The rectangle defining the new bounds of the view.
     */
    protected void setBounds(TRect bounds) {
        this.origin = new TPoint(bounds.a.x, bounds.a.y);
        this.size = new TPoint(bounds.b.x - bounds.a.x, bounds.b.y - bounds.a.y);
    }

    /**
     * Adds or removes {@code commands} from {@link #curCommandSet} based on {@code enable}.
     *
     * @param commands set of command identifiers to update
     * @param enable   {@code true} to enable commands, {@code false} to disable
     */
    public static void setCmdState(Set<Integer> commands, boolean enable) {
        if (enable) {
            enableCommands(commands);
        } else {
            disableCommands(commands);
        }
    }

    /**
     * Replaces the current command set for this view with the specified set.
     * <p>
     * If the new set differs from the existing one, the {@code commandSetChanged} flag is set.
     * A defensive copy of the provided set is stored internally.
     * </p>
     *
     * @param commands the new set of command identifiers to assign
     */
    public static void setCommands(Set<Integer> commands) {
        if (!curCommandSet.equals(commands)) {
            commandSetChanged = true;
        }
        curCommandSet = new HashSet<>(commands);
    }

    /** Sets the cursor position. */
    public void setCursor(int x, int y) {
        cursor.x = x;
        cursor.y = y;
        drawCursor();
    }

    /**
     * Restores this view's state from the provided source buffer.
     * <p>
     * The buffer must contain at least {@link #dataSize()} bytes. The default
     * implementation does nothing.
     * </p>
     *
     * @param src source buffer
     */
    public void setData(ByteBuffer src) {}

    /**
     * Sets or clears a state flag in the {@code TView.state} field.
     * <p>
     * The {@code state} parameter specifies the flag to modify (see {@code SF_XXX} constants),
     * and the {@code enable} parameter determines whether to turn the flag on ({@code true})
     * or off ({@code false}).
     * </p>
     * <p>
     * This method then performs any necessary actions to reflect the new state, such as redrawing views
     * that become exposed when visibility changes, or resetting the owner's current view if the state
     * affects visibility or selection.
     * </p>
     *
     * @param state The single-bit state flag to change.
     * @param enable {@code true} to enable the flag, {@code false} to disable it.
     * @throws IllegalArgumentException if more than one bit is set in {@code state}.
     */
    public void setState(int state, boolean enable) {
        if (Integer.bitCount(state) != 1) {
            throw new IllegalArgumentException("setState expects exactly one bit set in state");
        }

        logger.trace("{} TView@setState(state={}, enable={})", logName, state, enable);

        if (enable) {
            this.state |= state;
        } else {
            this.state &= ~state;
        }

        if (owner == null) return;

        switch (state) {
            case State.SF_VISIBLE:
                if ((owner.state & State.SF_EXPOSED) != 0) {
                    setState(State.SF_EXPOSED, enable);
                }
                if (enable) {
                    drawShow(null);
                } else {
                    drawHide(null);
                }
                if ((options & Options.OF_SELECTABLE) != 0) {
                    owner.resetCurrent();
                }
                break;
            case State.SF_CURSOR_VIS:
            case State.SF_CURSOR_INS:
                drawCursor();
                break;
            case State.SF_SHADOW:
                drawUnderView(true, null);
                break;
            case State.SF_FOCUSED:
                resetCursor();
                int command = enable ? Command.CM_RECEIVED_FOCUS : Command.CM_RELEASED_FOCUS;
                message(owner, TEvent.EV_BROADCAST, command, this);
                break;
            default:
                break;
        }
    }

    /**
     * Shows the view by calling {@link #setState} to set the {@code SF_VISIBLE} flag in {@code state}.
     */
    public void show() {
        logger.trace("{} TView@show()", logName);

        if ((state & State.SF_VISIBLE) == 0) {
            setState(State.SF_VISIBLE, true);
        }
    }

    /** Makes the text cursor visible. */
    public void showCursor() {
        logger.trace("{} TView@showCursor()", logName);

        setState(State.SF_CURSOR_VIS, true);
    }

    /**
     * Sets {@code min} and {@code max} to the minimum and maximum values that the {@code size}
     * field can assume.
     * <p>
     * This method is used by {@link #locate(TRect)} to ensure that a view's dimensions remain
     * within valid bounds. {@code locate} will not allow the view to be larger than these limits.
     * </p>
     * <p>
     * Defaults to minimum size (0,0) and maximum equal to the owner's size.
     * </p>
     *
     * @param min output parameter that receives the minimum allowed size
     * @param max output parameter that receives the maximum allowed size
     */
    public void sizeLimits(TPoint min, TPoint max) {
        min.x = 0;
        min.y = 0;
        if (owner != null) {
            max.x = owner.size.x;
            max.y = owner.size.y;
        } else {
            max.x = Short.MAX_VALUE;
            max.y = Short.MAX_VALUE;
        }
    }

    /**
     * Writes this view's persistent state to the supplied stream. The default
     * implementation does nothing.
     *
     * @param stream destination stream
     */
    public void store(TStream stream) {
        int saveState = state;
        // Do not persist transient runtime bits, mirroring Turbo Vision's behavior.
        state &= ~(State.SF_ACTIVE | State.SF_SELECTED | State.SF_FOCUSED | State.SF_EXPOSED);
        try {
            stream.writeInt(origin.x);
            stream.writeInt(origin.y);
            stream.writeInt(size.x);
            stream.writeInt(size.y);
            stream.writeInt(cursor.x);
            stream.writeInt(cursor.y);
            stream.writeInt(growModeToInt(growMode));
            stream.writeInt(dragMode);
            stream.writeInt(helpCtx);
            stream.writeInt(state);
            stream.writeInt(options);
            stream.writeInt(eventMask);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            state = saveState;
        }
    }

    /**
     * Reads a reference to another view within the group currently being
     * loaded. If the referenced view has not yet been created, a fixup is
     * registered and {@code null} is returned.
     *
     * @param stream source stream
     * @param fixup  optional {@link Consumer} that will receive the resolved view
     * @return the referenced view if already available; otherwise {@code null}
     */
    public TView getPeerViewPtr(TStream stream, Object fixup) throws IOException {
        int index = stream.readInt();
        if (index <= 0) {
            return null;
        }

        TGroup group = TGroup.getLoadingGroup();
        if (group == null) {
            return null;
        }

        TView view = group.at(index);
        if (view != null) {
            return view;
        }

        if (fixup instanceof Consumer<?>) {
            @SuppressWarnings("unchecked")
            Consumer<TView> consumer = (Consumer<TView>) fixup;
            group.addPeerFixup(index, consumer);
        }
        return null;
    }

    /**
     * Writes a reference to {@code view} relative to this view's owner.
     *
     * @param stream destination stream
     * @param view   view to reference
     */
    public void putPeerViewPtr(TStream stream, TView view) throws IOException {
        if (owner != null) {
            owner.putSubViewPtr(stream, view);
        } else {
            stream.writeInt(0);
        }
    }

    /**
     * Returns the current modal view, or null if none exists.
     */
    public TView topView() {
        if (theTopView == null) {
            TView p = this;
            while (p != null && (p.state & State.SF_MODAL) == 0) {
                p = p.owner;
            }
            return p;
        } else {
            return theTopView;
        }
    }

    /**
     * Checks whether the view is valid in its current state or after construction.
     * <p>
     * If {@code command} is {@code CM_VALID} (zero), this method verifies that the view
     * was successfully constructed and is ready for use. For any other command, it checks
     * the view's validity before a modal state ends (for example, before closing a window).
     * If the {@code OF_VALIDATE} option is set, it may also be called before releasing focus.
     * Subclasses can override this method to perform validation and optionally alert the user
     * if the view is invalid.
     * </p>
     * The default implementation always returns {@code true}.
     *
     * @param command validation context or command code
     * @return {@code true} if the view is valid, {@code false} otherwise
     */
    public boolean valid(int command) {
        return true;
    }

    /**
     * Writes a rectangular block of character cells to this view.
     * <p>
     * The buffer is expected to contain {@code w*h} entries where each entry is a
     * {@code short} value encoding the character in the low byte and the color
     * attribute in the high byte. The first {@code w} entries represent the
     * topmost row, the next {@code w} the following row and so on.
     * </p>
     *
     * @param x   starting column within the view
     * @param y   starting row within the view
     * @param w   width of the block in cells
     * @param h   height of the block in cells
     * @param buf buffer containing cells to write
     */
    protected void writeBuf(int x, int y, int w, int h, short[] buf) {
        if (buf == null || w <= 0 || h <= 0) return;
        for (int row = 0; row < h; row++) {
            writeView(y + row, x, w, buf, row * w);
        }
    }

    /**
     * Writes the same character cell repeatedly to the view.
     *
     * @param x     column within the view
     * @param y     row within the view
     * @param c     character to draw
     * @param color attribute byte describing foreground/background colors
     * @param count number of times to repeat
     */
    protected void writeChar(int x, int y, char c, int color, int count) {
        if (count <= 0) return;
        short[] line = new short[count];
        short cell = (short) (((color & 0xFF) << 8) | (c & 0xFF));
        Arrays.fill(line, cell);
        writeView(y, x, count, line, 0);
    }

    /**
     * Writes a horizontal line stored in {@code buf} repeatedly for {@code h} rows.
     *
     * @param x   column within the view
     * @param y   starting row within the view
     * @param w   width of the line in cells
     * @param h   number of rows to draw
     * @param buf buffer containing {@code w} cells representing a single line
     */
    protected void writeLine(int x, int y, int w, int h, short[] buf) {
        if (buf == null || w <= 0 || h <= 0) return;
        for (int row = 0; row < h; row++) {
            writeView(y + row, x, w, buf, 0);
        }
    }

    /**
     * Writes a string using the specified color attribute.
     *
     * @param x     column within the view
     * @param y     row within the view
     * @param str   string to draw
     * @param color attribute byte describing foreground/background colors
     */
    protected void writeStr(int x, int y, String str, int color) {
        if (str == null || str.isEmpty()) return;
        int len = str.length();
        short[] line = new short[len];
        for (int i = 0; i < len; i++) {
            line[i] = (short) (((color & 0xFF) << 8) | (str.charAt(i) & 0xFF));
        }
        writeView(y, x, len, line, 0);
    }

    /**
     * Writes a horizontal sequence of cells to this view after applying clipping
     * and owner translations.
     *
     * @param y       row within the view
     * @param x       starting column within the view
     * @param count   number of cells to write
     * @param buffer  source buffer containing the cells
     * @param offset  offset within {@code buffer} from which to start reading
     */
    private void writeView(int y, int x, int count, short[] buffer, int offset) {
        // Iteratively ascend the ownership chain, copying the affected region
        // into each ancestor's buffer while its lock flag remains cleared.
        final int required = State.SF_VISIBLE | State.SF_EXPOSED;

        TView view = this;
        int curY = y;
        int curX = x;
        int curCount = count;
        int curOffset = offset;
        short[] curBuffer = buffer;

        while (true) {
            if (view.owner == null || curBuffer == null || curCount <= 0) return;

            // Abort if the current view isn't both visible and exposed.
            if ((view.state & required) != required) return;

            // Clip vertically to the view's bounds
            if (curY < 0 || curY >= view.size.y) return;

            int start = curX;
            int end = curX + curCount;
            if (start < 0) {
                curOffset -= start;
                start = 0;
            }
            if (end > view.size.x) {
                end = view.size.x;
            }
            int length = end - start;
            if (length <= 0) return;

            int destX = view.origin.x + start;
            int destY = view.origin.y + curY;
            int bufIndex = curOffset + (start - curX);

            // Ascend the owner chain, clipping and translating until we reach
            // the nearest ancestor that owns a buffer or is locked.
            TGroup g = view.owner;
            TGroup top = null;
            while (g != null) {
                if ((g.state & required) != required) return;

                if (destY < g.clip.a.y || destY >= g.clip.b.y) return;
                int clipStart = Math.max(destX, g.clip.a.x);
                int clipEnd = Math.min(destX + length, g.clip.b.x);
                if (clipStart >= clipEnd) return;
                bufIndex += (clipStart - destX);
                length = clipEnd - clipStart;
                destX = clipStart;

                if (g.buffer != null || g.lockFlag != 0) {
                    top = g;
                    break;
                }

                destX += g.origin.x;
                destY += g.origin.y;
                g = g.owner;
            }

            if (top == null) return;
            IBuffer target = top.buffer;
            if (target == null) return;

            int available = Math.min(length, curBuffer.length - bufIndex);
            TPoint tmp = new TPoint();
            TPoint topOrigin = new TPoint(0, 0);
            top.makeGlobal(topOrigin, topOrigin);
            for (int i = 0; i < available; i++) {
                short cell = curBuffer[bufIndex + i];
                char ch = (char) (cell & 0xFF);
                int attr = (cell >>> 8) & 0xFF;
                int outAttr = attr;
                boolean covered = false;

                int globalX = topOrigin.x + destX + i;
                int globalY = topOrigin.y + destY;

                // For each ancestor level, check siblings drawn before this view
                TView child = view;
                for (TGroup parent = view.owner; parent != null && !covered; parent = parent.owner) {
                    for (TView s = parent.first(); s != null && s != child; s = s.nextView()) {
                        if ((s.state & State.SF_VISIBLE) == 0) continue;

                        tmp.x = 0;
                        tmp.y = 0;
                        s.makeGlobal(tmp, tmp);
                        int sx1 = tmp.x;
                        int sy1 = tmp.y;
                        int sx2 = sx1 + s.size.x;
                        int sy2 = sy1 + s.size.y;

                        if (globalY >= sy1 && globalY < sy2 && globalX >= sx1 && globalX < sx2) {
                            covered = true;
                            break;
                        }

                        if ((s.state & State.SF_SHADOW) != 0) {
                            int shx1 = sx1 + s.shadowSize.x;
                            int shy1 = sy1 + s.shadowSize.y;
                            int shx2 = shx1 + s.size.x;
                            int shy2 = shy1 + s.size.y;
                            if (globalY >= shy1 && globalY < shy2 && globalX >= shx1 && globalX < shx2) {
                                if (outAttr == attr) {
                                    outAttr = s.shadowAttr & 0xFF;
                                }
                            }
                        }
                    }
                    child = parent;
                }

                if (!covered) {
                    target.setChar(destX + i, destY, ch, outAttr);
                }
            }

            // If the ancestor isn't locked and has a further owner, propagate
            // the drawing upward. Otherwise we're done.
            if (top.lockFlag != 0 || top.owner == null) {
                return;
            }

            view = top;
            curY = destY;
            curX = destX;
            curCount = length;
            curBuffer = target.getData();
            curOffset = curY * view.size.x + curX;
        }
    }

    /**
     * Dispatches a message to the given receiver view.
     * <p>
     * This method constructs a {@code TEvent} with the specified parameters and passes it to
     * {@code handleEvent} of the receiver. If the receiver handles the event (i.e., clears it by
     * calling {@code clearEvent}), this method returns the possibly modified {@code infoPtr}
     * field from the event. Otherwise, it returns {@code null}.
     * </p>
     * <p>
     * Provides a standardized way to send command or information events to views.
     * </p>
     *
     * @param receiver the target view to receive the message
     * @param what the event class identifier
     * @param command the command code associated with the event
     * @param infoPtr optional additional information to attach to the event
     * @return the {@code infoPtr} if the receiver handled the event, or {@code null} otherwise
     */
    public static Object message(TView receiver, int what, int command, Object infoPtr) {
        if (receiver != null) {
            TEvent event = new TEvent();
            event.what = what;
            event.msg.command = command;
            event.msg.infoPtr = infoPtr;
            receiver.handleEvent(event);
            if (event.what == TEvent.EV_NOTHING) {
                return event.msg.infoPtr;
            }
        }
        return null;
    }

    public static Object message(TView receiver, int what, int command, int infoInt) {
        if (receiver != null) {
            TEvent event = new TEvent();
            event.what = what;
            event.msg.command = command;
            event.msg.infoInt = infoInt;
            receiver.handleEvent(event);
            if (event.what == TEvent.EV_NOTHING) {
                return event.msg.infoPtr;
            }
        }
        return null;
    }

    protected static char hotKey(String s) {
        if (s == null) {
            return 0;
        }
        boolean tilde = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '~') {
                tilde = !tilde;
            } else if (tilde) {
                return Character.toUpperCase(ch);
            }
        }
        return 0;
    }

    // Getters and setters

    /**
     * Returns the owner of this view.
     *
     * @return The {@link TGroup} that owns this view, or {@code null} if it has no owner.
     */
    public TGroup getOwner() {
        return owner;
    }

    /**
     * Sets the owner of this view.
     *
     * @param owner The {@link TGroup} to set as the owner of this view.
     */
    public void setOwner(TGroup owner) {
        this.owner = owner;
    }

    /**
     * Returns the next view in the sibling chain.
     *
     * @return The next {@link TView} in the sibling list, or {@code null} if this is the last.
     */
    public TView getNext() {
        return next;
    }

    public TPoint getSize() {
        return size;
    }

    public void setGrowModes(EnumSet<GrowMode> modes) {
        growMode.clear();
        if (modes != null) {
            growMode.addAll(modes);
        }
    }

    public void addGrowMode(GrowMode mode) {
        growMode.add(mode);
    }

    public void removeGrowMode(GrowMode mode) {
        growMode.remove(mode);
    }

    public void clearGrowModes() {
        growMode.clear();
    }

    public Set<GrowMode> getGrowModes() {
        return Collections.unmodifiableSet(EnumSet.copyOf(growMode));
    }

    /**
     * Retrieves the current option flags for this view.
     *
     * @return the view's current option flags
     */
    public int getOptions() {
        return options;
    }

    // Logging

    public String getLogName() {
        return logName;
    }

    @Override
    public String toString() {
        return logName;
    }

}
