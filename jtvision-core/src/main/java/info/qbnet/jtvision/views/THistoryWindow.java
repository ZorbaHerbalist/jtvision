package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.PaletteDescriptor;
import info.qbnet.jtvision.util.PaletteFactory;
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
        FRAME_PASSIVE(0x13),
        /** Active frame. */
        FRAME_ACTIVE(0x13),
        /** Frame icon. */
        FRAME_ICON(0x15),
        /** Scrollbar page area. */
        SCROLLBAR_PAGE(0x18),
        /** Scrollbar controls. */
        SCROLLBAR_CONTROLS(0x19),
        /** History viewer normal text. */
        VIEWER_NORMAL(0x13),
        /** History viewer selected text. */
        VIEWER_SELECTED(0x14);

        private final byte defaultValue;

        HistoryWindowColor(int defaultValue) {
            this.defaultValue = PaletteRole.toByte(defaultValue);
        }

        @Override
        public byte defaultValue() {
            return defaultValue;
        }
    }

    /** Palette reproducing Turbo Vision's {@code CHistoryWindow}. */
    public static final PaletteDescriptor<HistoryWindowColor> HISTORY_WINDOW_PALETTE =
            PaletteDescriptor.register("historyWindow", HistoryWindowColor.class);

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
        return HISTORY_WINDOW_PALETTE.palette();
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