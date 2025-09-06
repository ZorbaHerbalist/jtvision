package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;
import info.qbnet.jtvision.util.TPalette;

import java.util.HashSet;
import java.util.Set;
import java.io.IOException;

public class TWindow extends TGroup {

    public static final int CLASS_ID = 7;

    public static void registerType() {
        TStream.registerType(CLASS_ID, TWindow::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    public static class WindowFlag {
        public static final int WF_MOVE     = 0x01;
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

    public static final TPalette C_BLUE_WINDOW = new TPalette(TPalette.parseHexString("\\x08\\x09\\x0a\\x0b\\x0c\\x0d\\x0e\\x0f"));
    public static final TPalette C_CYAN_WINDOW = new TPalette(TPalette.parseHexString("\\x10\\x11\\x12\\x13\\x14\\x15\\x16\\x17"));
    public static final TPalette C_GRAY_WINDOW = new TPalette(TPalette.parseHexString("\\x18\\x19\\x1a\\x1b\\x1c\\x1d\\x1e\\x1f"));

    private WindowPalette palette = WindowPalette.WP_BLUE_WINDOW;

    public static final int WN_NO_NUMBER = 0;

    private static final TPoint minWinSize = new TPoint(16, 6);

    public TWindow(TRect bounds, String title, int number) {
        super(bounds);
        this.state |= State.SF_SHADOW;
        this.options |= Options.OF_SELECTABLE + Options.OF_TOP_SELECT;
        this.getGrowMode().addAll(GrowMode.GF_GROW_ALL);
        this.getGrowMode().add(GrowMode.GF_GROW_REL);
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
            zoomRect = new TRect(stream.readInt(), stream.readInt(), stream.readInt(), stream.readInt());
            number = stream.readInt();
            palette = WindowPalette.values()[stream.readInt()];
            frame = (TFrame) getSubViewPtr(stream);
            title = stream.readString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    public TPalette getPalette() {
        switch (palette) {
            case WP_BLUE_WINDOW:
                return C_BLUE_WINDOW;
            case WP_CYAN_WINDOW:
                return C_CYAN_WINDOW;
            case WP_GRAY_WINDOW:
            default:
                return C_GRAY_WINDOW;
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
                    if ((flags & (WindowFlag.WF_MOVE + WindowFlag.WF_GROW)) != 0) {
                        TRect limits = new TRect();
                        TPoint min = new TPoint();
                        TPoint max = new TPoint();
                        owner.getExtent(limits);
                        sizeLimits(min, max);
                        dragView(event, dragMode | (flags & (WindowFlag.WF_MOVE + WindowFlag.WF_GROW)), limits, min, max);
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
            if ((flags & (WindowFlag.WF_GROW + WindowFlag.WF_MOVE)) != 0) {
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
