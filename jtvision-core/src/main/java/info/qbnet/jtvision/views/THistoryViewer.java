package info.qbnet.jtvision.views;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.*;

/**
 * List viewer displaying the contents of a command history identified by an
 * integer id.  This class is a direct translation of Turbo Vision's
 * {@code THistoryViewer}.
 */
public class THistoryViewer extends TListViewer {

    /**
     * Palette roles for {@link THistoryViewer}.
     */
    public enum HistoryViewerColor implements PaletteRole {
        /** Active history entry. */
        ACTIVE(1, 0x06),
        /** Inactive history entry. */
        INACTIVE(2, 0x06),
        /** Focused entry highlight. */
        FOCUSED(3, 0x07),
        /** Selected entry highlight. */
        SELECTED(4, 0x06),
        /** Column divider. */
        DIVIDER(5, 0x06);

        private final int index;
        private final byte defaultValue;

        HistoryViewerColor(int index, int defaultValue) {
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

    /** Palette layout matching Turbo Vision's {@code CHistoryViewer}. */
    public static final PaletteDescriptor<HistoryViewerColor> HISTORY_VIEWER_PALETTE =
            PaletteDescriptor.register("historyViewer", HistoryViewerColor.class);

    /** Identifier of the history list shown by this viewer. */
    private final int historyId;

    public THistoryViewer(TRect bounds, TScrollBar hScrollBar, TScrollBar vScrollBar, int historyId) {
        super(bounds, 1, hScrollBar, vScrollBar);
        logger.debug("{} THistoryViewer@THistoryViewer(bounds={}, historyId={})", getLogName(), bounds, historyId);

        this.historyId = historyId;

        setRange(HistoryList.count(historyId));
        if (range > 1) {
            focusItem(1);
        }
        if (hScrollBar != null) {
            int width = historyWidth();
            int max = Math.max(1, width - size.x + 3);
            hScrollBar.setRange(1, max);
        }
    }

    @Override
    public TPalette getPalette() {
        return HISTORY_VIEWER_PALETTE.palette();
    }

    @Override
    protected String getText(int item, int maxLen) {
        return HistoryList.get(historyId, item);
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if ((event.what == TEvent.EV_MOUSE_DOWN && event.mouse.isDouble) ||
                (event.what == TEvent.EV_KEYDOWN && event.key.keyCode == KeyCode.KB_ENTER)) {
            endModal(Command.CM_OK);
            clearEvent(event);
        } else if ((event.what == TEvent.EV_KEYDOWN && event.key.keyCode == KeyCode.KB_ESC) ||
                (event.what == TEvent.EV_COMMAND && event.msg.command == Command.CM_CANCEL)) {
            endModal(Command.CM_CANCEL);
            clearEvent(event);
        }
    }

    private int historyWidth() {
        int width = 0;
        int count = HistoryList.count(historyId);
        for (int i = 0; i < count; i++) {
            String value = HistoryList.get(historyId, i);
            if (value.length() > width) {
                width = value.length();
            }
        }
        return width;
    }
}