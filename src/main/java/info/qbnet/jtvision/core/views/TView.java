package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TPoint;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.util.IBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for all visible user interface elements in the framework.
 * <p>
 * {@code TView} provides the foundational geometry, ownership, and linking functionality
 * common to all UI elements. It defines basic fields like position, size, and ownership,
 * and serves as the root of the view hierarchy.
 * </p>
 * <p>
 * {@code TView} is rarely instantiated directly. Instead, it is designed to be extended by
 * more specialized view types such as windows, buttons, scrollbars, or text editors.
 * </p>
 */
public class TView {

    /**
     * Reference to the {@link TGroup} object that owns this view.
     * <p>
     * If {@code null}, the view has no owner. The view is displayed within its owner's view
     * and will be clipped by the owner's bounding rectangle.
     * </p>
     */
    protected TGroup owner = null;

    /**
     * Reference to the next peer view in Z-order.
     * <p>
     * If this is the last subview, {@code next} points to the owner's first subview.
     * This field is used to navigate sibling views within the same owner group.
     * </p>
     */
    protected TView next = null;

    /**
     * The (X, Y) coordinates of the view’s top-left corner, relative to the owner’s origin.
     */
    protected TPoint origin;

    /**
     * The dimensions of the view.
     * <p>
     * The {@code x} and {@code y} components represent the width and height of the view,
     * i.e. the difference between the bottom-right and top-left coordinates.
     * </p>
     */
    protected TPoint size;

    /**
     * Defines how a view resizes when its owner changes size.
     * Each constant represents a growth behavior for a specific edge or scaling mode.
     */
    public static class GrowMode {
        /** If set, the left-hand side of the view will maintain a constant distance from its owner's
         * right-hand side. */
        public static final int GF_GROW_LO_X    = 1 << 0;
        /** If set, the top of the view will maintain a constant distance from the bottom of its owner. */
        public static final int GF_GROW_LO_Y    = 1 << 1;
        /** If set, the right-hand side of the view will maintain a constant distance from its owner's right side. */
        public static final int GF_GROW_HI_X    = 1 << 2;
        /** If set, the bottom edge of the view maintains a constant distance from the owner's bottom edge. */
        public static final int GF_GROW_HI_Y    = 1 << 3;
        /** If set, all four edges maintain a constant distance from the owner's edges, causing the view to grow or shrink with the owner's size. */
        public static final int GF_GROW_ALL     = GF_GROW_LO_X | GF_GROW_LO_Y | GF_GROW_HI_X | GF_GROW_HI_Y;
        /** If set, the view resizes in direct proportion to the owner's size, typically used for TWindow objects on the desktop. */
        public static final int GF_GROW_REL     = 1 << 4;
    }

    /** Current grow mode flags controlling how this view resizes with its owner. */
    protected int growMode = 0;

    /**
     * Collection of predefined help contexts that map view states to
     * context-sensitive help topics.
     */
    public static class HelpContext {
        /** Default context indicating that no help topic is associated. */
        public static final int HC_NO_CONTEXT   = 0;

        /** Context used while a view is being dragged. */
        public static final int HC_DRAGGING     = 1;
    }

    protected int helpCtx = HelpContext.HC_NO_CONTEXT;

    /**
     * Bit flags representing the runtime state of a view.
     * <p>
     * Each {@code SF_*} value defines a single state; multiple states can be
     * combined using the bitwise OR operator.
     * </p>
     */
    public static class State {
        /** Bit flag indicating the view is visible. */
        public static final int SF_VISIBLE      = 1 << 0;
        /** Bit flag showing the cursor is visible. */
        public static final int SF_CURSOR_VIS   = 1 << 1;
        /** Bit flag showing the cursor is in insert mode. */
        public static final int SF_CURSOR_INS   = 1 << 2;
        /** Bit flag that enables the view's shadow. */
        public static final int SF_SHADOW       = 1 << 3;
        /** Bit flag marking the view as active. */
        public static final int SF_ACTIVE       = 1 << 4;
        /** Bit flag marking the view as selected. */
        public static final int SF_SELECTED     = 1 << 5;
        /** Bit flag marking the view as focused. */
        public static final int SF_FOCUSED      = 1 << 6;
        /** Bit flag indicating the view is being dragged. */
        public static final int SF_DRAGGING     = 1 << 7;
        /** Bit flag marking the view as disabled. */
        public static final int SF_DISABLED     = 1 << 8;
        /** Bit flag indicating the view is modal. */
        public static final int SF_MODAL        = 1 << 9;
        /** Bit flag marking the view as the default choice. */
        public static final int SF_DEFAULT      = 1 << 10;
        /** Bit flag indicating the view is currently exposed. */
        public static final int SF_EXPOSED      = 1 << 11;
    }

    protected int state = State.SF_VISIBLE;

    /**
     * Bit flags that configure optional behavior of a view.
     * <p>
     * {@code OF_*} values may be combined with bitwise OR to enable multiple
     * options simultaneously.
     * </p>
     */
    public static class Options {
        /** Bit flag allowing the view to be selected. */
        public static final int OF_SELECTABLE   = 1 << 0;
        /** Bit flag giving the view top selection priority. */
        public static final int OF_TOP_SELECT   = 1 << 1;
        /** Bit flag enabling activation on the first mouse click. */
        public static final int OF_FIRST_CLICK  = 1 << 2;
        /** Bit flag drawing a frame around the view. */
        public static final int OF_FRAMED       = 1 << 3;
        /** Bit flag requesting pre-processing of events. */
        public static final int OF_PRE_PROCESS  = 1 << 4;
        /** Bit flag requesting post-processing of events. */
        public static final int OF_POST_PROCESS = 1 << 5;
        /** Bit flag for double-buffered drawing. */
        public static final int OF_BUFFERED     = 1 << 6;
        /** Bit flag allowing the view to tile its owner. */
        public static final int OF_TILEABLE     = 1 << 7;
        /** Bit flag centering the view horizontally. */
        public static final int OF_CENTER_X     = 1 << 8;
        /** Bit flag centering the view vertically. */
        public static final int OF_CENTER_Y     = 1 << 9;
        /** Combined bit flag for centering in both axes (OF_CENTER_X | OF_CENTER_Y). */
        public static final int OF_CENTER       = OF_CENTER_X | OF_CENTER_Y;
        /** Bit flag enabling validation before changes. */
        public static final int OF_VALIDATE     = 1 << 10;
    }

    protected int options = 0;

    /**
     * Bit mask that determines which event classes will be recognized by the view.
     * <p>
     * The default {@code eventMask} enables mouse down, key down, and command events.
     * Assigning {@code 0xFFFF} to {@code eventMask} causes the view to react to all event
     * classes. Conversely, a value of zero prevents the view from reacting to any events.
     * </p>
     */
    protected int eventMask;

    protected static final int ERROR_ATTR = 0xCF;

    /**
     * Explicit top view pointer; when non-null it overrides the automatic search.
     */
    protected static TView theTopView = null;

    /**
     * Current command set. All commands from {@code 0} through {@code 255} are
     * enabled except the standard window commands, namely
     * {@link Command#CM_ZOOM}, {@link Command#CM_CLOSE},
     * {@link Command#CM_RESIZE}, {@link Command#CM_NEXT} and
     * {@link Command#CM_PREV}.
     * <p>
     * See {@link Command} for the complete list of command identifiers.
     * </p>
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
     * Controls whether {@code handleEvent} calls emit trace logs.
     * <p>
     * This value can be overridden with the VM parameter
     * {@code -Djtvision.logEvents=false} to disable event logging.
     * </p>
     */
    protected static final boolean LOG_EVENTS =
            Boolean.parseBoolean(System.getProperty("jtvision.logEvents", "true"));

    /**
     * Offsets applied when drawing the view's shadow.
     * <p>
     * Represents the horizontal and vertical displacement of the shadow relative to the view.
     * </p>
     */
    private TPoint shadowSize = new TPoint(2, 1);

    /**
     * Attribute used when rendering the shadow.
     * <p>
     * Encodes the color pair that the screen buffer uses for the shadow.
     * </p>
     */
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

    private int growValue(int value, int s, int d) {
        if ((growMode & GrowMode.GF_GROW_REL) == 0)
            return value + d;
        else
            return (value * s + ((s - d) >> 1)) / (s - d);
    }

    /**
     * Calculates the new bounds of the view when the owner's size changes.
     * <p>
     * This method is called when the owner resizes and must compute the updated bounds of the view
     * given the change in the owner's size (specified by {@code delta}). The calculation is controlled
     * by the {@code growMode} flags, which determine how each side of the view reacts to the owner's
     * resizing. The resulting bounds are clamped by the view's minimum and maximum size limits.
     * </p>
     * <p>
     * When a view's owner changes size, the owner calls {@code calcBounds} and {@code changeBounds}
     * for all its subviews. {@code calcBounds} must calculate the new bounds using the flags specified
     * in {@code growMode}.
     * </p>
     *
     * @param bounds output rectangle that will contain the calculated bounds
     * @param delta the change in owner's size
     */
    public void calcBounds(TRect bounds, TPoint delta) {
        getBounds(bounds);

        int sx = owner.size.x;
        int dx = delta.x;
        if ((growMode & GrowMode.GF_GROW_LO_X) != 0) {
            bounds.a.x = growValue(bounds.a.x, sx, dx);
        }
        if ((growMode & GrowMode.GF_GROW_HI_X) != 0) {
            bounds.b.x = growValue(bounds.b.x, sx, dx);
        }
        if (bounds.b.x - bounds.a.x > TDrawBuffer.MAX_VIEW_LENGTH) {
            bounds.b.x = bounds.a.x + TDrawBuffer.MAX_VIEW_LENGTH;
        }

        int sy = owner.size.y;
        int dy = delta.y;
        if ((growMode & GrowMode.GF_GROW_LO_Y) != 0) {
            bounds.a.y = growValue(bounds.a.y, sy, dy);
        }
        if ((growMode & GrowMode.GF_GROW_HI_Y) != 0) {
            bounds.b.y = growValue(bounds.b.y, sy, dy);
        }

        TPoint min = new TPoint();
        TPoint max = new TPoint();
        sizeLimits(min, max);
        bounds.b.x = bounds.a.x + range(bounds.b.x - bounds.a.x, min.x, max.x);
        bounds.b.y = bounds.a.y + range(bounds.b.y - bounds.a.y, min.y, max.y);
    }

    /**
     * Changes the bounds of the view and redraws it.
     * <p>
     * This method must update the view's {@code origin} and {@code size} fields to match the
     * new rectangle provided in {@code bounds}. After updating, it triggers a redraw of the view.
     * </p>
     * <p>
     * This is an internal helper invoked by other {@code TView} methods and should not be called directly.
     * </p>
     *
     * @param bounds the new rectangle bounds for the view
     */
    public void changeBounds(TRect bounds) {
        logger.trace("{} TView@changeBounds(bounds={})", logName, bounds.toString());
        setBounds(bounds);
        drawView();
    }

    /**
     * Standard method used in {@code handleEvent} to signal that the view has successfully handled the event.
     * <p>
     * Sets {@code event.what} to {@code TEvent.EV_NOTHING} and {@code event.msg.infoPtr} to {@code this},
     * marking it as processed so it will not be handled again.
     * </p>
     *
     * @param event the {@link TEvent} to clear after handling
     */
    public void clearEvent(TEvent event) {
        event.what = TEvent.EV_NOTHING;
        event.msg.infoPtr = this;
    }

    /**
     * Determines whether a specific command is currently enabled for this view.
     * <p>
     * Returns {@code true} if the {@code command} is currently enabled; otherwise returns {@code false}.
     * When modal states change, commands can be enabled or disabled as needed. When returning
     * to a previous modal state, the original command set will be restored.
     * </p>
     *
     * @param command the command identifier to check
     * @return {@code true} if the command is enabled, {@code false} otherwise
     */
    public static boolean commandEnabled(int command) {
        return command <= 255 && curCommandSet.contains(command);
    }

    /**
     * Disables the specified set of commands for this view.
     * <p>
     * If any of the commands to be disabled are currently enabled, the {@code commandSetChanged}
     * flag is set to indicate that the command set has been modified. The specified commands are
     * then removed from the current command set.
     * </p>
     *
     * @param commands the set of command identifiers to disable
     */
    public static void disableCommands(Set<Integer> commands) {
        if (!Collections.disjoint(curCommandSet, commands)) {
            commandSetChanged = true;
        }
        curCommandSet.removeAll(commands);
    }

    /**
     * Finalizes the view's lifecycle by hiding it and requesting its owner to delete it.
     * <p>
     * {@code done} is called to remove the view from the screen and ensure it is properly
     * deleted by its owner.
     * </p>
     */
    protected void done() {
        logger.trace("{} TView@done()", logName);
        hide();
        if (owner != null) {
            owner.delete(this);
        }
    }

    /**
     * Called whenever the view must draw (display) itself.
     * <p>
     * This method must be overridden by each descendant to perform custom rendering.
     * It must draw the entire area of the view. Typically, this method is not called directly;
     * instead, {@link #drawView()} is used to draw only views that are exposed — that is,
     * partially or fully visible on the screen.
     * </p>
     * <p>
     * If needed, {@code draw()} may call {@code getClipRect()} to obtain
     * the region that needs redrawing, allowing for more efficient rendering of complex views.
     * </p>
     */
    public void draw() {
        logger.trace("{} TView@draw()", logName);

        TDrawBuffer buf = new TDrawBuffer();
        buf.moveChar(0,'w', getColor((short) 1), size.x);
        writeLine(0,0, size.x, size.y, buf.buffer);
    }

    /**
     * Redraws the region beneath this view when it becomes hidden.
     * <p>
     * Called from {@link #setState(int, boolean)} when the {@code SF_VISIBLE}
     * flag is cleared (e.g., via {@link #hide()}). It delegates to
     * {@link #drawUnderView(boolean, TView)} to refresh any exposed background
     * or shadowed area.
     * </p>
     *
     * @param lastView the last sibling view up to which the owner should
     *                 repaint; may be {@code null} to repaint all.
     */
    private void drawHide(TView lastView) {
        logger.trace("{} TView@drawHide(lastView={})", logName, lastView != null ? lastView.getLogName() : "null" );

//        drawCursor(); TODO
        drawUnderView((state & State.SF_SHADOW) != 0, lastView);
    }

    /**
     * Displays this view and optionally its shadow when it becomes visible.
     * <p>
     * Invoked by {@link #setState(int, boolean)} when the {@code SF_VISIBLE}
     * flag is enabled. It first draws the view via {@link #drawView()} and then
     * calls {@link #drawUnderView(boolean, TView)} to render any shadow.
     * </p>
     *
     * @param lastView the last sibling view up to which the owner should
     *                 repaint; may be {@code null} to repaint all.
     */
    private void drawShow(TView lastView) {
        logger.trace("{} TView@drawShow(lastView={})", logName, lastView != null ? lastView.getLogName() : "null" );

        drawView();
        if ((state & State.SF_SHADOW) != 0) {
            drawUnderView(true, lastView);
        }
    }

    /**
     * Repaints sibling views underneath a specified rectangle.
     * <p>
     * Used by {@link #drawUnderView(boolean, TView)} when parts of this view
     * are uncovered or when a shadow must be redrawn. It clips the owner's
     * drawing region to {@code rect} before delegating to
     * {@link TGroup#drawSubViews(TView, TView)}.
     * </p>
     *
     * @param rect     rectangle describing the region to repaint.
     * @param lastView last sibling view to stop repainting at; {@code null}
     *                 repaints all.
     */
    private void drawUnderRect(TRect rect, TView lastView) {
        owner.clip.intersect(rect);
        owner.drawSubViews(nextView(), lastView);
        owner.getExtent(owner.clip);
    }

    /**
     * Calculates the area beneath this view (and optionally its shadow) and
     * repaints the obscured siblings.
     * <p>
     * Serves as a helper for {@link #drawHide(TView)} and
     * {@link #drawShow(TView)}, bridging those private methods with the public
     * drawing routine {@link #drawView()}.
     * </p>
     *
     * @param doShadow {@code true} to include the shadow region when
     *                 repainting.
     * @param lastView the last sibling view up to which repainting should
     *                 occur; {@code null} repaints all.
     */
    private void drawUnderView(boolean doShadow, TView lastView) {
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

    /**
     * Calls {@code draw()} if {@link #exposed()} returns {@code true}, indicating that the view is exposed.
     * <p>
     * This is the preferred method to invoke when a view needs to be redrawn after changes
     * affecting its visual appearance. It ensures that drawing only occurs when the view is
     * (partially) visible on screen.
     * </p>
     */
    public void drawView() {
        logger.trace("{} TView@drawView()", logName);

        if (exposed()) {
            draw();
//            drawCursor(); TODO
        }
    }

    /**
     * Enables the specified set of commands for this view.
     * <p>
     * If any of the commands to be enabled are not already present in the current command set,
     * the {@code commandSetChanged} flag is set to indicate a modification. The specified commands
     * are then added to the current command set.
     * </p>
     *
     * @param commands the set of command identifiers to enable
     */
    public static void enableCommands(Set<Integer> commands) {
        if (!curCommandSet.containsAll(commands)) {
            commandSetChanged = true;
        }
        curCommandSet.addAll(commands);
    }

    /**
     * Terminates the current modal state and returns {@code command} as the result of the
     * {@code execView} function call that created the modal state.
     * <p>
     * This implementation finds the topmost view and calls its {@code endModal} method
     * with the given command. If no top view exists, no action is taken.
     * </p>
     *
     * @param command the command value to return from the modal state
     */
    public void endModal(int command) {
        TView p = topView();
        if (p != null) {
            p.endModal(command);
        }
    }

    /**
     * Called from {@code TGroup.execView} whenever a view becomes modal.
     * <p>
     * If a view supports modal execution, it should override this method to implement its own event loop.
     * The result returned by {@code execute} becomes the value returned from {@code TGroup.execView}.
     * </p>
     * <p>
     * The default implementation immediately returns {@link Command#CM_CANCEL}.
     * </p>
     *
     * @return the command result of executing the view
     */
    public int execute() {
        return Command.CM_CANCEL;
    }

    /**
     * Determines whether a horizontal segment of this view is visible.
     * <p>
     * Used internally by the public {@link #exposed()} method to check if a
     * given row of the view is entirely covered by higher Z-order siblings.
     * </p>
     *
     * @param y      the absolute Y coordinate of the row being tested.
     * @param xStart starting X coordinate of the segment (inclusive).
     * @param xEnd   ending X coordinate of the segment (exclusive).
     * @return {@code true} if any character cell in the range is exposed; otherwise
     *         {@code false}.
     */
    private boolean isRowExposed(int y, int xStart, int xEnd) {
        if (owner == null || owner.last == null) return true;

        TView target = this;
        TView current = owner.last.getNext();
        do {
            if (current == target) break;

            if ((current.getState() & State.SF_VISIBLE) != 0) {
                int cy = current.origin.y;
                int ch = current.size.y;
                if (y >= cy && y < cy + ch) {
                    int cx1 = current.origin.x;
                    int cx2 = cx1 + current.size.x;

                    // Case: current covers left part of our row
                    if (cx1 <= xStart && cx2 > xStart) {
                        xStart = cx2;
                        if (xStart >= xEnd) return false;
                    }
                    // Case: current cuts into right part
                    else if (cx1 < xEnd && cx2 >= xEnd) {
                        xEnd = cx1;
                        if (xStart >= xEnd) return false;
                    }
                    // Case: current entirely covers [xStart, xEnd)
                    else if (cx1 <= xStart && cx2 >= xEnd) {
                        return false;
                    }
                }
            }
            current = current.getNext();
        } while (current != owner.last.getNext());

        return xStart < xEnd;
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
        if ((state & State.SF_VISIBLE) == 0) return false;
        if ((size.x <= 0) || (size.y <= 0)) return false;

        for (int y = 0; y < size.y; y++) {
            int rowY = origin.y + y;
            int xStart = origin.x;
            int xEnd = origin.x + size.x;

            // Clip to owner's clip rect
            if (owner == null) return false;
            TRect clip = owner.clip;
            if (rowY < clip.a.y || rowY >= clip.b.y) continue;

            int visibleXStart = Math.max(clip.a.x, xStart);
            int visibleXEnd = Math.min(clip.b.x, xEnd);

            if (visibleXStart >= visibleXEnd) continue;

            if (isRowExposed(rowY, visibleXStart, visibleXEnd)) return true;
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

    /**
     * Sets the extent rectangle of the view into the provided {@code extent} parameter.
     * <p>
     * The {@code a} point is set to (0, 0), and the {@code b} point is set to the current size
     * of the view. This represents the local coordinate space of the view itself.
     * </p>
     *
     * @param extent The {@link TRect} instance to be populated with the extent.
     */
    public void getExtent(TRect extent) {
        extent.a = new TPoint(0, 0);
        extent.b = new TPoint(size.x, size.y);
    }

    /**
     * Returns the help context identifier for this view.
     * <p>
     * By default, returns the value stored in the view's {@code helpCtx} field.
     * If the view is currently being dragged (SF_DRAGGING flag set), returns
     * {@link HelpContext#HC_DRAGGING} instead.
     * </p>
     *
     * @return the current help context identifier
     */
    public int getHelpCtx() {
        if ((state & State.SF_DRAGGING) != 0) {
            return HelpContext.HC_DRAGGING;
        } else {
            return helpCtx;
        }
    }

    /**
     * Returns the palette for this view.
     * <p>
     * {@code getPalette()} must return a reference to the view's palette, or {@code null}
     * if the view has no palette.
     * </p>
     * <p>
     * {@code getPalette()} is called by methods like {@code getColor()}, {@code writeChar()}, and
     * {@code writeStr()} (not yet implemented) when converting palette indexes to actual
     * character attributes. A return value of {@code null} indicates that no color translation
     * should be performed by this view.
     * </p>
     * <p>
     * This method is almost always overridden in descendant object types. The default
     * implementation returns {@code null}.
     * </p>
     */
    public TPalette getPalette() {
        return null;
    }

    /**
     * Central method through which all event handling is implemented.
     * <p>
     * The {@code what} field of the {@link TEvent} parameter contains the event class ({@code TEvent.EV_XXXX}),
     * and the remaining event fields further describe the event. To indicate that it has handled
     * an event, {@code handleEvent} should call {@link #clearEvent(TEvent)}.
     * </p>
     * <p>
     * This base implementation handles {@code TEvent.EV_MOUSE_DOWN} events by checking whether the
     * view is not selected and not disabled, and whether it is selectable. If so, it attempts
     * to select itself by calling {@link #focus()}. No other events are handled here.
     * </p>
     *
     * @param event the event to process
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

    /**
     * Moves the view to the top of its owner's subview list.
     * <p>
     * Equivalent to calling {@code putInFrontOf(owner.first())}.
     * </p>
     */
    public void makeFirst() {
        putInFrontOf(owner.first());
    }

    /**
     * Converts the {@code source} point coordinates from local (view) coordinates to
     * global (screen) coordinates and stores the result in {@code dest}.
     * <p>
     * This method walks up the ownership chain, adding each view's origin offset to
     * the coordinates until the top-level owner is reached. {@code source} and
     * {@code dest} may refer to the same object.
     * </p>
     *
     * @param source the point in local coordinates
     * @param dest the destination point to receive the global coordinates
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
     * Converts the {@code source} point coordinates from global (screen) coordinates to
     * local (view) coordinates and stores the result in {@code dest}.
     * <p>
     * Useful for converting a {@code TEvent.EV_MOUSE} event's {@code where} field from
     * global coordinates to local coordinates. For example:
     * {@code makeLocal(event.where, mouseLoc)}.
     * This method walks up the ownership chain, subtracting each view's origin offset from
     * the coordinates until the top-level owner is reached. {@code source} and
     * {@code dest} may refer to the same object.
     * </p>
     *
     * @param source the point in global coordinates
     * @param dest the destination point to receive the local coordinates
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

    /**
     * Convert a color value into an attribute using the palette chain.
     * @param color color index (1..n)
     * @return attribute value
     */
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

    /**
     * Maps a color pair into an attribute pair.
     * @param colorPair low byte foreground, high byte background
     * @return mapped attribute pair
     */
    private short mapCPair(short colorPair) {
        int background = (colorPair >> 8) & 0xff;
        int foreground = colorPair & 0xff;
        if (background != 0) {
            background = mapColor(background);
        }
        foreground = mapColor(foreground);
        return (short) (background << 8 | foreground & 0xff);
    }

    /**
     * Returns the next mouse event in the {@code event} argument.
     * <p>
     * This method repeatedly calls {@code getEvent} until an event occurs whose type matches the
     * specified {@code mask} or is a mouse button release ({@code TEvent.EV_MOUSE_UP}). It then stores the
     * event in the {@code event} parameter and returns {@code true} if it matched the mask, or
     * {@code false} if a mouse button release occurred.
     * </p>
     * <p>
     * Tracks mouse movement while a button is pressed, useful for operations like block selection.
     * </p>
     *
     * @param event the event structure to be filled with the received event
     * @param mask the mask specifying which mouse events to wait for
     * @return {@code true} if a matching event occurred, {@code false} if a mouse button release occurred
     */
    public boolean mouseEvent(TEvent event, int mask) {
        do {
            getEvent(event);
        } while ((event.what & (mask | TEvent.EV_MOUSE_UP)) == 0);
        return event.what != TEvent.EV_MOUSE_UP;
    }

    /**
     * Returns {@code true} if the specified mouse position (given in global/screen coordinates)
     * lies within the calling view's boundaries.
     * <p>
     * The method converts the mouse position to the view's local coordinates using
     * {@link #makeLocal(TPoint, TPoint)}, obtains the view's extent rectangle via
     * {@link #getExtent(TRect)}, and checks whether the converted point is contained within it.
     * </p>
     * @param mouse the mouse position in global coordinates
     * @return {@code true} if the mouse is inside the view, {@code false} otherwise
     */
    public boolean mouseInView(TPoint mouse) {
        TRect extent = new TRect();
        TPoint local = new TPoint(mouse.x, mouse.y);
        makeLocal(mouse, local);
        getExtent(extent);
        return extent.contains(local);
    }

    /**
     * Returns a reference to the next subview in the owner's subview list.
     * <p>
     * {@code null} is returned if the calling view is the last one in its owner's list.
     * </p>
     *
     * @return The next subview, or {@code null} if this is the last in the list.
     */
    public TView nextView() {
        if (owner.last == this) {
            return null;
        } else {
            return next;
        }
    }

    /**
     * Returns a reference to the previous subview in the owner's subview list.
     * <p>
     * If the calling view is the first one in its owner's list, {@code prev()} returns the last view
     * in the list. Note that {@code prev()} treats the list as circular, whereas {@code prevView()}
     * treats the list linearly.
     * </p>
     *
     * @return The previous {@link TView}, or {@code null} if this is the only view in the list.
     */
    public TView prev() {
        TView previous = this;
        TView np = next;

        while ((np != null) && (np != this)) {
            previous = np;
            np = np.next;
        }

        return previous;
    }

    /**
     * Puts the given event into the event queue, making it the next event returned by {@code getEvent}.
     * <p>
     * Only one event can be pushed onto the event queue in this way. Often used by views to generate
     * command events, for example:
     * <pre>
     * Event.what = TEvent.EV_COMMAND;
     * Event.command = Command.CM_SAVE_ALL;
     * Event.infoPtr = null;
     * putEvent(Event);
     * </pre>
     * The default implementation forwards the event to the view's owner.
     * </p>
     *
     * @param event the event to enqueue
     */
    public void putEvent(TEvent event) {
        if (owner != null) {
            owner.putEvent(event);
        }
    }

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
                    // TODO
                    // owner.resetCursor()
                }
            }
        }
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
                if ((owner.getState() & State.SF_EXPOSED) != 0) {
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

//            case State.SF_CURSOR_VIS:
//            case State.SF_CURSOR_INS:
//                drawCursor(null);
//                break;
//
            case State.SF_SHADOW:
                drawUnderView(true, null);
                break;
//
//            case State.SF_FOCUSED:
//                resetCursor();
//                int command = enable ? cmReceivedFocus : cmReleasedFocus;
//                message(owner, Event.BROADCAST, command, this);
//                break;

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

    public TPoint getSize() {
        return size;
    }

    /**
     * Returns the next view in the sibling chain.
     *
     * @return The next {@link TView} in the sibling list, or {@code null} if this is the last.
     */
    public TView getNext() {
        return next;
    }

    /**
     * Retrieves the current state flags for this view.
     *
     * @return the view's current state flags
     */
    public int getState() {
        return state;
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
