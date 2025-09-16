package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.PaletteRole;
import info.qbnet.jtvision.util.TPalette;
import info.qbnet.jtvision.util.TRect;

/**
 * Simple window embedding a {@link THistoryViewer}.  The implementation follows
 * Turbo Vision's {@code THistoryWindow} closely.
 */
public class THistoryWindow extends TWindow {

    /** Palette roles for {@link THistoryWindow}. */
    public enum HistoryWindowColor implements PaletteRole {
        /** Passive frame. */
        FRAME_PASSIVE(1),
        /** Active frame. */
        FRAME_ACTIVE(2),
        /** Frame icon. */
        FRAME_ICON(3),
        /** Scrollbar page area. */
        SCROLLBAR_PAGE(4),
        /** Scrollbar controls. */
        SCROLLBAR_CONTROLS(5),
        /** History viewer normal text. */
        VIEWER_NORMAL(6),
        /** History viewer selected text. */
        VIEWER_SELECTED(7);

        private final int index;

        HistoryWindowColor(int index) {
            this.index = index;
        }

        @Override
        public int index() {
            return index;
        }
    }

    /** Palette reproducing Turbo Vision's {@code CHistoryWindow}. */
    public static final TPalette C_HISTORY_WINDOW = new TPalette(
            TPalette.mapFromHexString("\\x13\\x13\\x15\\x18\\x19\\x13\\x14", HistoryWindowColor.values()));

    /** Embedded viewer presenting the available history items. */
    private THistoryViewer viewer;

    public THistoryWindow(TRect bounds, int historyId) {
        super(bounds, "", WN_NO_NUMBER);
        logger.debug("{} THistoryWindow@THistoryWindow(bounds={}, historyId={})", getLogName(), bounds, historyId);

        this.flags = WindowFlag.WF_CLOSE;
        initViewer(historyId);
    }

    @Override
    public TPalette getPalette() {
        return C_HISTORY_WINDOW;
    }

    /** Returns the text of the currently focused history item. */
    public String getSelection() {
        if (viewer == null || viewer.range == 0) {
            return "";
        }
        return viewer.getText(viewer.focused, 255);
    }

    /** Creates the history viewer and inserts it into the window. */
    protected void initViewer(int historyId) {
        TRect r = new TRect();
        getExtent(r);
        r.grow(-1, -1);
        viewer = new THistoryViewer(r,
                standardScrollBar(ScrollBarOptions.SB_HORIZONTAL | ScrollBarOptions.SB_HANDLE_KEYBOARD),
                standardScrollBar(ScrollBarOptions.SB_VERTICAL | ScrollBarOptions.SB_HANDLE_KEYBOARD),
                historyId);
        insert(viewer);
    }
}