package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;
import info.qbnet.jtvision.util.CString;
import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.TPalette;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class TCluster extends TView {

    public int value = 0;
    public int sel = 0;
    public int enableMask = 0xFFFFFFFF;
    public final List<String> strings = new ArrayList<>();

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

    /**
     * Loads a cluster from the provided {@link TStream}.
     */
    protected TCluster(TStream stream) {
        super(stream);
        try {
            value = stream.readInt();
            sel = stream.readInt();
            enableMask = stream.readInt();
            int count = stream.readInt();
            for (int i = 0; i < count; i++) {
                strings.add(stream.readString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Re-evaluate selectable option based on enable mask.
        setButtonState(0, true);
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

    @Override
    public int dataSize() {
        return 2;
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

    /** Maps a local mouse position to the corresponding item index. */
    private int findSel(TPoint p) {
        TRect r = new TRect();
        getExtent(r);
        if (!r.contains(p)) {
            return -1;
        }
        int i = 0;
        while (p.x >= column(i + size.y)) {
            i += size.y;
        }
        int s = i + p.y;
        if (s >= strings.size()) {
            return -1;
        }
        return s;
    }

    @Override
    public void getData(ByteBuffer dst) {
        dst.put((byte) (value & 0xFF));
        dst.put((byte) ((value >>> 8) & 0xFF));
    }

    @Override
    public TPalette getPalette() {
        return C_CLUSTER;
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if ((options & Options.OF_SELECTABLE) == 0) {
            return;
        }

        if (event.what == TEvent.EV_MOUSE_DOWN) {
            TPoint mouse = new TPoint();
            makeLocal(event.mouse.where, mouse);
            int i = findSel(mouse);
            if (i != -1 && buttonState(i)) {
                sel = i;
            }
            drawView();

            do {
                makeLocal(event.mouse.where, mouse);
                if (findSel(mouse) == sel) {
                    showCursor();
                } else {
                    hideCursor();
                }
            } while (mouseEvent(event, TEvent.EV_MOUSE_MOVE));

            showCursor();
            makeLocal(event.mouse.where, mouse);
            if (findSel(mouse) == sel && buttonState(sel)) {
                press(sel);
                drawView();
            }
            clearEvent(event);
        } else if (event.what == TEvent.EV_KEYDOWN) {
            int s = sel;
            int i;
            switch (KeyCode.ctrlToArrow(event.key.keyCode)) {
                case KeyCode.KB_UP:
                    if ((state & State.SF_FOCUSED) != 0) {
                        i = 0;
                        do {
                            i++;
                            s--;
                            if (s < 0) {
                                s = strings.size() - 1;
                            }
                        } while (!buttonState(s) && i <= strings.size());
                        if (i <= strings.size()) {
                            sel = s;
                            movedTo(sel);
                            drawView();
                        }
                        clearEvent(event);
                    }
                    break;
                case KeyCode.KB_DOWN:
                    if ((state & State.SF_FOCUSED) != 0) {
                        i = 0;
                        do {
                            i++;
                            s++;
                            if (s >= strings.size()) {
                                s = 0;
                            }
                        } while (!buttonState(s) && i <= strings.size());
                        if (i <= strings.size()) {
                            sel = s;
                            movedTo(sel);
                            drawView();
                        }
                        clearEvent(event);
                    }
                    break;
                case KeyCode.KB_RIGHT:
                    if ((state & State.SF_FOCUSED) != 0) {
                        i = 0;
                        do {
                            i++;
                            s += size.y;
                            if (s >= strings.size()) {
                                s = (s + 1) % size.y;
                                if (s >= strings.size()) {
                                    s = 0;
                                }
                            }
                        } while (!buttonState(s) && i <= strings.size());
                        if (i <= strings.size()) {
                            sel = s;
                            movedTo(sel);
                            drawView();
                        }
                        clearEvent(event);
                    }
                    break;
                case KeyCode.KB_LEFT:
                    if ((state & State.SF_FOCUSED) != 0) {
                        i = 0;
                        do {
                            i++;
                            if (s > 0) {
                                s -= size.y;
                                if (s < 0) {
                                    s = ((strings.size() + size.y - 1) / size.y) * size.y + s - 1;
                                    if (s >= strings.size()) {
                                        s = strings.size() - 1;
                                    }
                                }
                            } else {
                                s = strings.size() - 1;
                            }
                        } while (!buttonState(s) && i <= strings.size());
                        if (i <= strings.size()) {
                            sel = s;
                            movedTo(sel);
                            drawView();
                        }
                        clearEvent(event);
                    }
                    break;
                default:
                    for (i = 0; i < strings.size(); i++) {
                        char c = hotKey(strings.get(i));
                        boolean match = (KeyCode.getAltCode(c) == event.key.keyCode)
                                || (((owner != null && owner.phase == TGroup.Phase.POST_PROCESS)
                                || (state & State.SF_FOCUSED) != 0)
                                && c != 0 && Character.toUpperCase(event.key.charCode) == c);
                        if (match) {
                            if (buttonState(i)) {
                                if (focus()) {
                                    sel = i;
                                    movedTo(sel);
                                    press(sel);
                                    drawView();
                                }
                                clearEvent(event);
                            }
                            return;
                        }
                    }
                    if (event.key.charCode == ' ' && (state & State.SF_FOCUSED) != 0 && buttonState(sel)) {
                        press(sel);
                        drawView();
                        clearEvent(event);
                    }
                    break;
            }
        }
    }

    /** Indicates whether the given item is marked. Subclasses override. */
    protected boolean mark(int item) {
        return false;
    }

    /** Notifies subclasses when the selection cursor moves to a new item. */
    protected void movedTo(int item) {
        // default: do nothing
    }
    /** Converts the mark state into an index for the marker string. */
    protected int multiMark(int item) {
        return mark(item) ? 1 : 0;
    }

    /** Invoked when an item is activated (e.g., pressed). */
    protected void press(int item) {
        // default: do nothing
    }

    /** Returns the row (y offset) for the specified item. */
    protected int row(int item) {
        return item % size.y;
    }

    /** Enables or disables buttons specified by {@code mask}. */
    public void setButtonState(int mask, boolean enable) {
        if (enable) {
            enableMask |= mask;
        } else {
            enableMask &= ~mask;
        }
        if (strings.size() <= 32) {
            boolean selectable = false;
            int bits = enableMask;
            for (int i = 0; i < strings.size(); i++) {
                if ((bits & 1) != 0) {
                    selectable = true;
                    break;
                }
                bits >>>= 1;
            }
            if (selectable) {
                options |= Options.OF_SELECTABLE;
            } else {
                options &= ~Options.OF_SELECTABLE;
            }
        }
    }

    @Override
    public void setData(ByteBuffer src) {
        int lo = src.get() & 0xFF;
        int hi = src.get() & 0xFF;
        value = (hi << 8) | lo;
        drawView();
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            stream.writeInt(value);
            stream.writeInt(sel);
            stream.writeInt(enableMask);
            stream.writeInt(strings.size());
            for (String s : strings) {
                stream.writeString(s);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
