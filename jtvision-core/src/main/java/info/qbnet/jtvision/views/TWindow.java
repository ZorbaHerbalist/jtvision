package info.qbnet.jtvision.views;

import com.fasterxml.jackson.databind.node.ObjectNode;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TWindow extends TGroup {

    public static final int CLASS_ID = 7;

    /**
     * Palette roles for {@link TWindow} color sets.
     */
    public enum WindowColor implements PaletteRole {
        /** Frame when window is inactive. */
        FRAME_PASSIVE,
        /** Frame when window is active. */
        FRAME_ACTIVE,
        /** Frame icon area. */
        FRAME_ICON,
        /** Scrollbar page area. */
        SCROLLBAR_PAGE,
        /** Scrollbar controls. */
        SCROLLBAR_CONTROLS,
        /** Scroller normal text. */
        SCROLLER_NORMAL,
        /** Scroller selected text. */
        SCROLLER_SELECTED,
        /** Reserved slot. */
        RESERVED;
    }

    public static void registerType() {
        TStream.registerType(CLASS_ID, TWindow::new);
        JsonViewStore.registerType(TWindow.class, TWindow::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    public static class WindowFlag {
        public static final int WF_MOVE     = 0x01;
        /** Enables resizing and implies {@link #WF_MOVE}. */
        public static final int WF_GROW     = 0x02;
        public static final int WF_CLOSE    = 0x04;
        public static final int WF_ZOOM     = 0x08;
    }

    protected int flags;

    private TRect zoomRect = new TRect();
    private TFrame frame;
    private String title;
    protected int number;

    public enum WindowPalette {
        WP_BLUE_WINDOW,
        WP_CYAN_WINDOW,
        WP_GRAY_WINDOW;
    }

    public static final PaletteDescriptor<WindowColor> BLUE_WINDOW_PALETTE =
            PaletteDescriptor.register("window.blue", WindowColor.class);
    public static final PaletteDescriptor<WindowColor> CYAN_WINDOW_PALETTE =
            PaletteDescriptor.register("window.cyan", WindowColor.class);
    public static final PaletteDescriptor<WindowColor> GRAY_WINDOW_PALETTE =
            PaletteDescriptor.register("window.gray", WindowColor.class);

    private WindowPalette palette = WindowPalette.WP_BLUE_WINDOW;

    public static final int WN_NO_NUMBER = 0;

    private static final TPoint minWinSize = new TPoint(16, 6);

    public static class ScrollBarOptions {
        public static final int SB_HORIZONTAL = 0x0000;
        public static final int SB_VERTICAL = 0x0001;
        public static final int SB_HANDLE_KEYBOARD = 0x0002;
    }

    public TWindow(TRect bounds, String title, int number) {
        super(bounds);
        this.state |= State.SF_SHADOW;
        this.options |= Options.OF_SELECTABLE + Options.OF_TOP_SELECT;
        setGrowModes(GrowMode.growAll());
        addGrowMode(GrowMode.GF_GROW_REL);
        this.flags = WindowFlag.WF_MOVE | WindowFlag.WF_GROW | WindowFlag.WF_CLOSE | WindowFlag.WF_ZOOM;
        this.title = title;
        this.number = number;
        initFrame();
        if (frame != null) {
            insert(frame);
        }
        getBounds(zoomRect);
    }

    public TWindow(TStream stream) {
        super(stream);
        try {
            flags = stream.readInt();
            if ((flags & WindowFlag.WF_GROW) != 0) {
                flags |= WindowFlag.WF_MOVE;
            }
            zoomRect = new TRect(stream.readInt(), stream.readInt(), stream.readInt(), stream.readInt());
            number = stream.readInt();
            palette = WindowPalette.values()[stream.readInt()];
            frame = (TFrame) getSubViewPtr(stream);
            title = stream.readString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TWindow(ObjectNode node) {
        super(node);
        flags = JsonUtil.getInt(node, "flags", 0);
        if ((flags & WindowFlag.WF_GROW) != 0) {
            flags |= WindowFlag.WF_MOVE;
        }
        ObjectNode zoom = node.has("zoomRect") && node.get("zoomRect").isObject()
                ? (ObjectNode) node.get("zoomRect") : null;
        TPoint a = JsonUtil.getPoint(zoom, "a", new TPoint(0, 0));
        TPoint b = JsonUtil.getPoint(zoom, "b", new TPoint(0, 0));
        zoomRect = new TRect(a.x, a.y, b.x, b.y);
        number = JsonUtil.getInt(node, "number", 0);
        int paletteIndex = JsonUtil.getInt(node, "palette", WindowPalette.WP_BLUE_WINDOW.ordinal());
        WindowPalette[] palettes = WindowPalette.values();
        if (paletteIndex < 0 || paletteIndex >= palettes.length) {
            paletteIndex = WindowPalette.WP_BLUE_WINDOW.ordinal();
        }
        palette = palettes[paletteIndex];
        int frameIndex = JsonUtil.getInt(node, "frame", 0);
        if (frameIndex > 0) {
            TView view = getSubViewPtr(frameIndex);
            if (view instanceof TFrame) {
                frame = (TFrame) view;
            }
        }
        title = JsonUtil.getString(node, "title");
    }

    public void close() {
        logger.trace("{} TWindow@close()", getLogName());
        if (valid(Command.CM_CLOSE)) {
            done();
        }
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            stream.writeInt(flags);
            stream.writeInt(zoomRect.a.x);
            stream.writeInt(zoomRect.a.y);
            stream.writeInt(zoomRect.b.x);
            stream.writeInt(zoomRect.b.y);
            stream.writeInt(number);
            stream.writeInt(palette.ordinal());
            putSubViewPtr(stream, frame);
            stream.writeString(title);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void storeJson(ObjectNode node) {
        super.storeJson(node);
        node.put("flags", flags);
        ObjectNode zoom = node.putObject("zoomRect");
        JsonUtil.putPoint(zoom, "a", zoomRect.a);
        JsonUtil.putPoint(zoom, "b", zoomRect.b);
        node.put("number", number);
        node.put("palette", palette.ordinal());
        node.put("frame", getSubViewIndex(frame));
        if (title != null) {
            node.put("title", title);
        } else {
            node.putNull("title");
        }
    }

    @Override
    public TPalette getPalette() {
        switch (palette) {
            case WP_BLUE_WINDOW:
                return BLUE_WINDOW_PALETTE.palette();
            case WP_CYAN_WINDOW:
                return CYAN_WINDOW_PALETTE.palette();
            case WP_GRAY_WINDOW:
            default:
                return GRAY_WINDOW_PALETTE.palette();
        }
    }

    public String getTitle(int maxSize) {
        if (title == null) {
            return "";
        }
        return title;
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_COMMAND) {
            switch (event.msg.command) {
                case Command.CM_RESIZE:
                    if ((flags & WindowFlag.WF_MOVE) != 0) {
                        TRect limits = new TRect();
                        TPoint min = new TPoint();
                        TPoint max = new TPoint();
                        owner.getExtent(limits);
                        sizeLimits(min, max);
                        boolean canGrow = (flags & WindowFlag.WF_GROW) != 0;
                        dragView(event, canGrow);
                        clearEvent(event);
                    }
                    break;
                case Command.CM_CLOSE:
                    if (((flags & WindowFlag.WF_CLOSE) != 0) && (event.msg.infoPtr == null || event.msg.infoPtr == this)) {
                        clearEvent(event);
                        if ((state & State.SF_MODAL) == 0) {
                            close();
                        } else {
                            event.what = TEvent.EV_COMMAND;
                            event.msg.command = Command.CM_CANCEL;
                            putEvent(event);
                            clearEvent(event);
                        }
                    }
                    break;
                case Command.CM_ZOOM:
                    if (((flags & WindowFlag.WF_ZOOM) != 0) && (event.msg.infoPtr == null || event.msg.infoPtr == this)) {
                        zoom();
                        clearEvent(event);
                    }
                    break;
            }
        } else if (event.what == TEvent.EV_KEYDOWN) {
            switch (event.key.keyCode) {
                case KeyCode.KB_TAB:
                    focusNext(false);
                    clearEvent(event);
                    break;
                case KeyCode.KB_SHIFT_TAB:
                    focusNext(true);
                    clearEvent(event);
                    break;
            }
        } else if (event.what == TEvent.EV_BROADCAST && event.msg.command == Command.CM_SELECT_WINDOW_NUM && event.msg.infoInt == number && (options & Options.OF_SELECTABLE) != 0) {
            select();
            clearEvent(event);
        }
    }

    public void initFrame() {
        TRect r = new TRect();
        getExtent(r);
        frame = new TFrame(r);
    }

    @Override
    public void setState(int state, boolean enable) {
        super.setState(state, enable);
        if (state == State.SF_SELECTED) {
            setState(State.SF_ACTIVE, enable);
        }
        if (state == State.SF_SELECTED || (state == State.SF_EXPOSED && (this.state & State.SF_SELECTED) != 0)) {
            Set<Integer> windowCommands = new HashSet<>();
            windowCommands.add(Command.CM_NEXT);
            windowCommands.add(Command.CM_PREV);
            if ((flags & WindowFlag.WF_MOVE) != 0) {
                windowCommands.add(Command.CM_RESIZE);
            }
            if ((flags & WindowFlag.WF_CLOSE) != 0) {
                windowCommands.add(Command.CM_CLOSE);
            }
            if ((flags & WindowFlag.WF_ZOOM) != 0) {
                windowCommands.add(Command.CM_ZOOM);
            }
            if (enable) {
                enableCommands(windowCommands);
            } else {
                disableCommands(windowCommands);
            }
        }
    }

    @Override
    public void sizeLimits(TPoint min, TPoint max) {
        super.sizeLimits(min, max);
        min.x = minWinSize.x;
        min.y = minWinSize.y;
    }

    public TScrollBar standardScrollBar(int options) {
        TRect r = new TRect();
        getExtent(r);
        if ((options & ScrollBarOptions.SB_VERTICAL) == 0) {
            r.assign(r.a.x + 2, r.b.y - 1, r.b.x - 2, r.b.y);
        } else {
            r.assign(r.b.x - 1, r.a.y + 1, r.b.x, r.b.y - 1);
        }
        TScrollBar s = new TScrollBar(r);
        insert(s);
        if ((options & ScrollBarOptions.SB_HANDLE_KEYBOARD) != 0) {
            s.options |= Options.OF_POST_PROCESS;
        }
        return s;
    }

    public void zoom() {
        TPoint min = new TPoint();
        TPoint max = new TPoint();
        sizeLimits(min, max);
        if (size.x != max.x || size.y != max.y) {
            getBounds(zoomRect);
            TRect r = new TRect(0, 0, max.x, max.y);
            locate(r);
        } else {
            locate(zoomRect);
        }
    }
}
