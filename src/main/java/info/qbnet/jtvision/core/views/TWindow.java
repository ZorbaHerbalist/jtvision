package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.objects.TPoint;
import info.qbnet.jtvision.core.objects.TRect;

import java.util.HashSet;
import java.util.Set;

public class TWindow extends TGroup {

    public static class WindowFlag {
        public static final int WF_MOVE     = 0x01;
        public static final int WF_GROW     = 0x02;
        public static final int WF_CLOSE    = 0x04;
        public static final int WF_ZOOM     = 0x08;
    }

    protected static int flags;

    private TRect zoomRect = new TRect();
    private TFrame frame;
    private String title;
    protected int number;

    public enum Palette {
        WF_BLUE_WINDOW,
        WF_CYAN_WINDOW,
        WF_GRAY_WINDOW;
    }

    public static final TPalette C_BLUE_WINDOW = new TPalette(TPalette.parseHexString("\\x08\\x09\\x0a\\x0b\\x0c\\x0d\\x0e\\x0f"));
    public static final TPalette C_CYAN_WINDOW = new TPalette(TPalette.parseHexString("\\x10\\x11\\x12\\x13\\x14\\x15\\x16\\x17"));
    public static final TPalette C_GRAY_WINDOW = new TPalette(TPalette.parseHexString("\\x18\\x19\\x1a\\x1b\\x1c\\x1d\\x1e\\x1f"));

    private Palette palette = Palette.WF_BLUE_WINDOW;

    public static final int WN_NO_NUMBER = 0;

    private static final TPoint minWinSize = new TPoint(16, 6);

    public TWindow(TRect bounds, String title, int number) {
        super(bounds);
        this.state |= State.SF_SHADOW;
        this.options |= Options.OF_SELECTABLE + Options.OF_TOP_SELECT;
        this.growMode |= GrowMode.GF_GROW_ALL + GrowMode.GF_GROW_REL;
        this.flags = WindowFlag.WF_MOVE | WindowFlag.WF_GROW | WindowFlag.WF_CLOSE | WindowFlag.WF_ZOOM;
        this.title = title;
        this.number = number;
        initFrame();
        if (frame != null) {
            insert(frame);
        }
        getBounds(zoomRect);
    }

    @Override
    public TPalette getPalette() {
        switch (palette) {
            case WF_BLUE_WINDOW:
                return C_BLUE_WINDOW;
            case WF_CYAN_WINDOW:
                return C_CYAN_WINDOW;
            case WF_GRAY_WINDOW:
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
}
