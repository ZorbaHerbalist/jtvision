package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.core.objects.TPoint;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.util.IBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for all visible user interface elements in the Turbo Vision-style framework.
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
    private TGroup owner = null;

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
     * Bit flags representing the runtime state of a view.
     * <p>
     * Each {@code SF_*} value defines a single state; multiple states can be
     * combined using the bitwise OR operator.
     * </p>
     */
    public class State {
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
     * Bit flags that configure optional behaviour of a view.
     * <p>
     * {@code OF_*} values may be combined with bitwise OR to enable multiple
     * options simultaneously.
     * </p>
     */
    public class Options {
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
        /** Bit flag centring the view horizontally. */
        public static final int OF_CENTER_X     = 1 << 8;
        /** Bit flag centring the view vertically. */
        public static final int OF_CENTER_Y     = 1 << 9;
        /** Combined bit flag for centring in both axes (OF_CENTER_X | OF_CENTER_Y). */
        public static final int OF_CENTER       = OF_CENTER_X | OF_CENTER_Y;
        /** Bit flag enabling validation before changes. */
        public static final int OF_VALIDATE     = 1 << 10;
    }

    protected int options = 0;

    protected static final int ERROR_ATTR = 0xCF;

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
            r.b.x++; // TODO ShadowSize.X
            r.b.y++; // TODO ShadowSize.Y
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
     * Determines whether a horizontal segment of this view is visible.
     * <p>
     * Used internally by the public {@link #exposed()} method to check if a
     * given row of the view is entirely covered by higher Z-order siblings.
     * </p>
     *
     * @param y      the absolute Y coordinate of the row being tested.
     * @param xStart starting X coordinate of the segment (inclusive).
     * @param xEnd   ending X coordinate of the segment (exclusive).
     * @return {@code true} if any pixel in the range is exposed; otherwise
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
     * Hides the view by calling {@link #setState} to clear the {@code SF_VISIBLE} flag in {@code state}.
     */
    public void hide() {
        logger.trace("{} TView@hide()", logName);

        if ((state & State.SF_VISIBLE) != 0) {
            setState(State.SF_VISIBLE, false);
        }
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
        TView current = null;
        TView candidate = null;

        while (true) {
            TView previous = candidate;
            candidate = candidate.next;

            // If we reached the end or looped back to the current view
            if (candidate == null || candidate == current) {
                break;
            }

            // Check if this view points to the current one
            if (candidate.next == current) {
                return candidate;
            }
        }

        // No previous view found (e.g., this is the only item in the list)
        return null;
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
    private void setBounds(TRect bounds) {
        this.origin = bounds.a;
        this.size = new TPoint(bounds.b.x - bounds.a.x, bounds.b.y - bounds.a.y);
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
//            case State.SF_SHADOW:
//                drawUnderView(true, null);
//                break;
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
     * Writes a rectangular block of character cells to this view.
     * <p>
     * The buffer is expected to contain {@code w*h} entries where each entry is a
     * {@code short} value encoding the character in the low byte and the colour
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
     * @param color attribute byte describing foreground/background colours
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
     * Writes a string using the specified colour attribute.
     *
     * @param x     column within the view
     * @param y     row within the view
     * @param str   string to draw
     * @param color attribute byte describing foreground/background colours
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
        if (owner == null || buffer == null || count <= 0) return;

        // Clip vertically to this view's bounds
        if (y < 0 || y >= size.y) return;

        int start = x;
        int end = x + count;
        if (start < 0) {
            offset += -start;
            start = 0;
        }
        if (end > size.x) {
            end = size.x;
        }
        int length = end - start;
        if (length <= 0) return;

        int destX = origin.x + start;
        int destY = origin.y + y;
        int bufIndex = offset + (start - x);

        TGroup g = owner;
        // Traverse up the owner chain applying clipping and coordinate translation
        while (true) {
            // Clip against current group's clipping rectangle
            if (destY < g.clip.a.y || destY >= g.clip.b.y) return;
            int clipStart = Math.max(destX, g.clip.a.x);
            int clipEnd = Math.min(destX + length, g.clip.b.x);
            if (clipStart >= clipEnd) return;
            bufIndex += (clipStart - destX);
            length = clipEnd - clipStart;
            destX = clipStart;

            // Stop when we've reached the top-most group
            if (g.getOwner() == null) {
                break;
            }

            destX += g.origin.x;
            destY += g.origin.y;
            g = g.getOwner();
        }

        IBuffer target = g.buffer;
        if (target == null) return;

        int available = Math.min(length, buffer.length - bufIndex);
        for (int i = 0; i < available; i++) {
            short cell = buffer[bufIndex + i];
            char ch = (char) (cell & 0xFF);
            int attr = (cell >>> 8) & 0xFF;
            target.setChar(destX + i, destY, ch, attr);
        }
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
