package info.qbnet.jtvision.views;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.*;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Base class for scrollable views linking horizontal and vertical scroll bars.
 *
 * <p>This is a direct Java translation of Turbo Vision's {@code TScroller}
 * object.  It tracks the current scroll offsets ("delta") and view limits,
 * updating associated {@link TScrollBar} instances and requesting redraws when
 * the position changes.</p>
 */
public class TScroller extends TView {

    public static final int CLASS_ID = 4;

    /**
     * Palette roles for {@link TScroller}.
     */
    public enum ScrollerColor implements PaletteRole {
        /** Normal text. */
        NORMAL_TEXT(1),
        /** Selected text. */
        SELECTED_TEXT(2);

        private final int index;

        ScrollerColor(int index) {
            this.index = index;
        }

        @Override
        public int index() {
            return index;
        }
    }

    public static void registerType() {
        TStream.registerType(CLASS_ID, TScroller::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    /** Horizontal scroll bar or {@code null} if none. */
    protected TScrollBar hScrollBar;
    /** Vertical scroll bar or {@code null} if none. */
    protected TScrollBar vScrollBar;

    /** Current scroll offset. */
    protected TPoint delta = new TPoint();
    /** Maximum scroll limits. */
    protected TPoint limit = new TPoint();

    /** Counter preventing redraws while performing batch operations. */
    protected int drawLock = 0;
    /** Flag indicating that a redraw is pending once {@link #drawLock} drops to zero. */
    protected boolean drawFlag = false;

    public static final TPalette C_SCROLLER;

    static {
        PaletteFactory.registerDefaults("scroller", ScrollerColor.class, "\\x06\\x07");
        C_SCROLLER = PaletteFactory.get("scroller");
    }

    public TScroller(TRect bounds, TScrollBar hScrollBar, TScrollBar vScrollBar) {
        super(bounds);
        logger.debug("{} TScroller@TScroller(bounds={}, hScrollBar={}, vScrollBar={})",
                getLogName(), bounds, hScrollBar, vScrollBar);

        options |= Options.OF_SELECTABLE;
        eventMask |= TEvent.EV_BROADCAST;
        this.hScrollBar = hScrollBar;
        this.vScrollBar = vScrollBar;
    }

    public TScroller(TStream stream) {
        super(stream);
        try {
            this.hScrollBar = (TScrollBar) getPeerViewPtr(stream, (Consumer<TView>) v -> this.hScrollBar = (TScrollBar) v);
            this.vScrollBar = (TScrollBar) getPeerViewPtr(stream, (Consumer<TView>) v -> this.vScrollBar = (TScrollBar) v);
            this.delta = new TPoint(stream.readInt(), stream.readInt());
            this.limit = new TPoint(stream.readInt(), stream.readInt());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void changeBounds(TRect bounds) {
        logger.trace("{} TScroller@changeBounds(bounds={})", getLogName(), bounds);
        setBounds(bounds);
        drawLock++;
        setLimit(limit.x, limit.y);
        drawLock--;
        drawFlag = false;
        drawView();
    }

    /** Redraws the view if a draw was deferred while locked. */
    protected void checkDraw() {
        if (drawLock == 0 && drawFlag) {
            drawFlag = false;
            drawView();
        }
    }

    @Override
    public TPalette getPalette() {
        return C_SCROLLER;
    }

    @Override
    public void handleEvent(TEvent event) {
        logger.trace("{} TScroller@handleEvent(event={})", getLogName(), event);
        super.handleEvent(event);
        if (event.what == TEvent.EV_BROADCAST &&
                event.msg.command == Command.CM_SCROLLBAR_CHANGED &&
                (event.msg.infoPtr == hScrollBar || event.msg.infoPtr == vScrollBar)) {
            scrollDraw();
        }
    }

    /** Updates the view when scroll bar values change. */
    protected void scrollDraw() {
        TPoint d = new TPoint();
        d.x = (hScrollBar != null) ? hScrollBar.value : 0;
        d.y = (vScrollBar != null) ? vScrollBar.value : 0;
        if (d.x != delta.x || d.y != delta.y) {
            setCursor(cursor.x + delta.x - d.x, cursor.y + delta.y - d.y);
            delta.x = d.x;
            delta.y = d.y;
            if (drawLock != 0) {
                drawFlag = true;
            } else {
                drawView();
            }
        }
    }

    /** Scrolls to the specified position by updating the scroll bars. */
    public void scrollTo(int x, int y) {
        drawLock++;
        if (hScrollBar != null) {
            hScrollBar.setValue(x);
        }
        if (vScrollBar != null) {
            vScrollBar.setValue(y);
        }
        drawLock--;
        checkDraw();
    }

    /** Sets the maximum scrollable limits and adjusts the scroll bars. */
    public void setLimit(int x, int y) {
        limit.x = x;
        limit.y = y;
        drawLock++;
        if (hScrollBar != null) {
            hScrollBar.setParams(hScrollBar.value, 0, x - size.x, size.x - 1, hScrollBar.arStep);
        }
        if (vScrollBar != null) {
            vScrollBar.setParams(vScrollBar.value, 0, y - size.y, size.y - 1, vScrollBar.arStep);
        }
        drawLock--;
        checkDraw();
    }

    @Override
    public void setState(int aState, boolean enable) {
        logger.trace("{} TScroller@setState(state={}, enable={})", getLogName(), aState, enable);
        super.setState(aState, enable);
        if ((aState & (State.SF_ACTIVE | State.SF_SELECTED)) != 0) {
            showSBar(hScrollBar);
            showSBar(vScrollBar);
        }
    }

    private void showSBar(TScrollBar sBar) {
        if (sBar != null) {
            if (getState(State.SF_ACTIVE | State.SF_SELECTED)) {
                sBar.show();
            } else {
                sBar.hide();
            }
        }
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            putPeerViewPtr(stream, hScrollBar);
            putPeerViewPtr(stream, vScrollBar);
            stream.writeInt(delta.x);
            stream.writeInt(delta.y);
            stream.writeInt(limit.x);
            stream.writeInt(limit.y);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

