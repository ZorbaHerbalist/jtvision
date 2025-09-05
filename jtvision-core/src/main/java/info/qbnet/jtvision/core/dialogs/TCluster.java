package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TDrawBuffer;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;
import info.qbnet.jtvision.util.CString;

import java.util.ArrayList;
import java.util.List;

public abstract class TCluster extends TView {

    protected int value = 0;
    protected int sel = 0;
    protected int enableMask = 0xFFFFFFFF;
    private final List<String> strings = new ArrayList<>();

    public static final TPalette C_CLUSTER = new TPalette(TPalette.parseHexString("\\x10\\x11\\x12\\x12\\x1f"));

    TCluster(TRect bounds, List<String> strings) {
        super(bounds);
        this.options |= (Options.OF_SELECTABLE + Options.OF_FIRST_CLICK + Options.OF_PRE_PROCESS + Options.OF_POST_PROCESS);

        if (strings != null) {
            this.strings.addAll(strings);
        }

        setCursor(2, 0);
        showCursor();
    }

    /** Determines whether the item is enabled using {@link #enableMask}. */
    protected boolean buttonState(int item) {
        if (item < 0 || item >= 32) {
            return true;
        }
        return (enableMask & (1 << item)) != 0;
    }

    /** Returns the column (x offset) for the specified item. */
    protected int column(int item) {
        if (item < size.y) {
            return 0;
        }

        int width = 0;
        int col = -6;
        for (int i = 0; i <= item; i++) {
            if (i % size.y == 0) {
                col += width + 6;
                width = 0;
            }
            if (i < strings.size()) {
                int l = CString.cStrLen(strings.get(i));
                if (l > width) {
                    width = l;
                }
            }
        }
        return col;
    }

    /**
     * Renders a columnar list of check/radio boxes.
     * <p>
     * Each item consists of an {@code icon} (e.g. "( )" or
     * "[ ]"), a marker character selected from the {@code marker} string
     * depending on {@link #multiMark(int)} and the associated text from
     * {@link #strings}. Disabled items use {@code enableMask} to determine
     * their color.
     * </p>
     *
     * @param icon   framing characters for an item
     * @param marker characters representing unmarked/marked states
     */
    protected void drawMultiBox(String icon, String marker) {
        // Resolve colors for normal, selected and disabled items
        short cNorm = getColor((short) 0x0301);
        short cSel  = getColor((short) 0x0402);
        short cDis  = getColor((short) 0x0505);

        TDrawBuffer buf = new TDrawBuffer();

        for (int i = 0; i < size.y; i++) {
            buf.moveChar(0, ' ', cNorm & 0xFF, size.x);

            // Iterate over columns
            int columns = (strings.size() - 1) / size.y + 1;
            for (int j = 0; j < columns; j++) {
                int cur = j * size.y + i;
                if (cur >= strings.size()) {
                    continue;
                }

                int col = column(cur);
                int textLen = CString.cStrLen(strings.get(cur));

                if (col + textLen + 5 < TDrawBuffer.MAX_VIEW_LENGTH && col < size.x) {
                    short color;
                    if (!buttonState(cur)) {
                        color = cDis;
                    } else if (cur == sel && (state & State.SF_FOCUSED) != 0) {
                        color = cSel;
                    } else {
                        color = cNorm;
                    }

                    buf.moveChar(col, ' ', color & 0xFF, size.x - col);
                    buf.moveStr(col, icon, color & 0xFF);
                    int markIdx = Math.min(multiMark(cur), marker.length() - 1);
                    char markCh = marker.charAt(markIdx);
                    buf.buffer[col + 2] = (short) ((buf.buffer[col + 2] & 0xFF00) | (markCh & 0xFF));
                    buf.moveCStr(col + 5, strings.get(cur), color);

                    if (showMarkers && (state & State.SF_FOCUSED) != 0 && cur == sel) {
                        buf.buffer[col] = (short) ((buf.buffer[col] & 0xFF00) | SPECIAL_CHARS[0]);
                        int nextCol = column(cur + size.y) - 1;
                        if (nextCol >= 0 && nextCol < TDrawBuffer.MAX_VIEW_LENGTH) {
                            buf.buffer[nextCol] = (short) ((buf.buffer[nextCol] & 0xFF00) | SPECIAL_CHARS[1]);
                        }
                    }
                }
            }

            writeLine(0, i, size.x, 1, buf.buffer);
        }

        setCursor(column(sel) + 2, row(sel));
    }

    @Override
    public TPalette getPalette() {
        return C_CLUSTER;
    }

    /** Indicates whether the given item is marked. Subclasses override. */
    protected boolean mark(int item) {
        return false;
    }

    /** Converts the mark state into an index for the marker string. */
    protected int multiMark(int item) {
        return mark(item) ? 1 : 0;
    }

    /** Returns the row (y offset) for the specified item. */
    protected int row(int item) {
        return item % size.y;
    }

}
