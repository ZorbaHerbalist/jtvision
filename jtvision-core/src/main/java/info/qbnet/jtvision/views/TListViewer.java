package info.qbnet.jtvision.views;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.*;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Scrollable list view translated from Turbo Vision's {@code TListViewer}.
 *
 * <p>The widget displays a list of items arranged in one or more columns and
 * supports optional horizontal and vertical scroll bars.  Subclasses must
 * provide the item text via {@link #getText(int, int)} and may override
 * {@link #isSelected(int)} and {@link #selectItem(int)} to implement custom
 * selection logic.</p>
 */
public class TListViewer extends TView {

    public static final int CLASS_ID = 5;

    /**
     * Palette roles for {@link TListViewer}.
     */
    public enum ListViewerColor implements PaletteRole {
        /** Active list background. */
        ACTIVE,
        /** Inactive list background. */
        INACTIVE,
        /** Focused item. */
        FOCUSED,
        /** Selected item. */
        SELECTED,
        /** Column divider. */
        DIVIDER;
    }

    /** Registers this class for stream persistence. */
    public static void registerType() {
        TStream.registerType(CLASS_ID, TListViewer::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    /** Horizontal scroll bar or {@code null} if none. */
    protected TScrollBar hScrollBar;
    /** Vertical scroll bar or {@code null} if none. */
    protected TScrollBar vScrollBar;

    /** Number of columns to display. */
    protected int numCols;
    /** Index of the topmost item currently visible. */
    protected int topItem;
    /** Index of the focused item. */
    protected int focused;
    /** Total number of items. */
    protected int range;

    /** Palette describing active, inactive, focused, selected and divider colors. */
    public static final PaletteDescriptor<ListViewerColor> LIST_VIEWER_PALETTE =
            PaletteDescriptor.register("listViewer", ListViewerColor.class);

    public TListViewer(TRect bounds, int numCols, TScrollBar hScrollBar, TScrollBar vScrollBar) {
        super(bounds);
        logger.debug("{} TListViewer@TListViewer(bounds={}, numCols={}, hScrollBar={}, vScrollBar={})",
                getLogName(), bounds, numCols, hScrollBar, vScrollBar);

        options |= Options.OF_FIRST_CLICK | Options.OF_SELECTABLE;
        eventMask |= TEvent.EV_BROADCAST;

        this.range = 0;
        this.numCols = numCols;
        this.topItem = 0;
        this.focused = 0;

        if (vScrollBar != null) {
            int pgStep, arStep;
            if (numCols == 1) {
                pgStep = size.y - 1;
                arStep = 1;
            } else {
                pgStep = size.y * numCols;
                arStep = size.y;
            }
            vScrollBar.setStep(pgStep, arStep);
        }
        if (hScrollBar != null) {
            hScrollBar.setStep(size.x / numCols, 1);
        }
        this.hScrollBar = hScrollBar;
        this.vScrollBar = vScrollBar;
    }

    public TListViewer(TStream stream) {
        super(stream);
        try {
            this.hScrollBar = (TScrollBar) getPeerViewPtr(stream, (Consumer<TView>) v -> this.hScrollBar = (TScrollBar) v);
            this.vScrollBar = (TScrollBar) getPeerViewPtr(stream, (Consumer<TView>) v -> this.vScrollBar = (TScrollBar) v);
            this.numCols = stream.readInt();
            this.topItem = stream.readInt();
            this.focused = stream.readInt();
            this.range = stream.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void changeBounds(TRect bounds) {
        logger.trace("{} TListViewer@changeBounds(bounds={})", getLogName(), bounds);
        super.changeBounds(bounds);
        if (hScrollBar != null) {
            hScrollBar.setStep(size.x / Math.max(numCols, 1), hScrollBar.arStep);
        }
        if (vScrollBar != null) {
            vScrollBar.setStep(size.y, vScrollBar.arStep);
        }
    }

    @Override
    public void draw() {
        logger.trace("{} TListViewer@draw()", getLogName());

        short normalColor, selectedColor, focusedColor, color;
        if ((state & (State.SF_SELECTED | State.SF_ACTIVE)) ==
                (State.SF_SELECTED | State.SF_ACTIVE)) {
            normalColor = getColor(ListViewerColor.ACTIVE);
            focusedColor = getColor(ListViewerColor.FOCUSED);
            selectedColor = getColor(ListViewerColor.SELECTED);
        } else {
            normalColor = getColor(ListViewerColor.INACTIVE);
            focusedColor = getColor(ListViewerColor.FOCUSED);
            selectedColor = getColor(ListViewerColor.SELECTED);
        }

        int indent = (hScrollBar != null) ? hScrollBar.value : 0;
        int colWidth = size.x / numCols + 1;
        TDrawBuffer b = new TDrawBuffer();

        for (int i = 0; i < size.y; i++) {
            for (int j = 0; j < numCols; j++) {
                int item = j * size.y + i + topItem;
                int curCol = j * colWidth;
                int scOff;

                if ((state & (State.SF_SELECTED | State.SF_ACTIVE)) ==
                        (State.SF_SELECTED | State.SF_ACTIVE) &&
                        focused == item && range > 0) {
                    color = focusedColor;
                    setCursor(curCol + 1, i);
                    scOff = 0;
                } else if (item < range && isSelected(item)) {
                    color = selectedColor;
                    scOff = 2;
                } else {
                    color = normalColor;
                    scOff = 4;
                }

                b.moveChar(curCol, ' ', color, colWidth);
                if (item < range) {
                    String text = getText(item, colWidth + indent);
                    if (indent < text.length()) {
                        text = text.substring(indent);
                    } else {
                        text = "";
                    }
                    if (text.length() > colWidth) {
                        text = text.substring(0, colWidth);
                    }
                    b.moveStr(curCol + 1, text, color);
                    if (showMarkers) {
                        b.buffer[curCol] = (short) ((b.buffer[curCol] & 0xFF00) | SPECIAL_CHARS[scOff]);
                        b.buffer[curCol + colWidth - 2] =
                                (short) ((b.buffer[curCol + colWidth - 2] & 0xFF00) | SPECIAL_CHARS[scOff + 1]);
                    }
                }
                b.moveChar(curCol + colWidth - 1, (char) 0xB3, getColor(ListViewerColor.DIVIDER), 1);
            }
            writeLine(0, i, size.x, 1, b.buffer);
        }
    }

    /** Sets the focused item and adjusts scrolling if necessary. */
    public void focusItem(int item) {
        focused = item;
        if (vScrollBar != null) {
            vScrollBar.setValue(item);
        }
        if (item < topItem) {
            if (numCols == 1) {
                topItem = item;
            } else {
                topItem = item - item % size.y;
            }
        } else if (item >= topItem + (size.y * numCols)) {
            if (numCols == 1) {
                topItem = item - size.y + 1;
            } else {
                topItem = item - item % size.y - (size.y * (numCols - 1));
            }
        }
    }

    /** Clamps {@code item} to the valid range and focuses it. */
    protected void focusItemNum(int item) {
        if (item < 0) {
            item = 0;
        } else if (item >= range && range > 0) {
            item = range - 1;
        }
        if (range != 0) {
            focusItem(item);
        }
    }

    @Override
    public TPalette getPalette() {
        return LIST_VIEWER_PALETTE.palette();
    }

    /** Returns the text for {@code item}. Subclasses must override. */
    protected String getText(int item, int maxLen) {
        throw new UnsupportedOperationException();
    }

    /** Determines if {@code item} should be drawn as selected. */
    protected boolean isSelected(int item) {
        return item == focused;
    }

    @Override
    public void handleEvent(TEvent event) {
        logger.trace("{} TListViewer@handleEvent(event={})", getLogName(), event);
        super.handleEvent(event);

        final int MouseAutosToSkip = 4;

        if (event.what == TEvent.EV_MOUSE_DOWN) {
            TPoint mouse = new TPoint();
            int colWidth = size.x / numCols + 1;
            int oldItem = focused;
            int newItem;
            makeLocal(event.mouse.where, mouse);
            if (mouseInView(event.mouse.where)) {
                newItem = mouse.y + (size.y * (mouse.x / colWidth)) + topItem;
            } else {
                newItem = oldItem;
            }
            int count = 0;
            do {
                if (newItem != oldItem) {
                    focusItemNum(newItem);
                    drawView();
                }
                oldItem = newItem;
                makeLocal(event.mouse.where, mouse);
                if (mouseInView(event.mouse.where)) {
                    newItem = mouse.y + (size.y * (mouse.x / colWidth)) + topItem;
                } else {
                    if (numCols == 1) {
                        if (event.what == TEvent.EV_MOUSE_AUTO) {
                            count++;
                        }
                        if (count == MouseAutosToSkip) {
                            count = 0;
                            if (mouse.y < 0) {
                                newItem = focused - 1;
                            } else if (mouse.y >= size.y) {
                                newItem = focused + 1;
                            }
                        }
                    } else {
                        if (event.what == TEvent.EV_MOUSE_AUTO) {
                            count++;
                        }
                        if (count == MouseAutosToSkip) {
                            count = 0;
                            if (mouse.x < 0) {
                                newItem = focused - size.y;
                            } else if (mouse.x >= size.x) {
                                newItem = focused + size.y;
                            } else if (mouse.y < 0) {
                                newItem = focused - focused % size.y;
                            } else if (mouse.y > size.y) {
                                newItem = focused - focused % size.y + size.y - 1;
                            }
                        }
                    }
                }
            } while (mouseEvent(event, TEvent.EV_MOUSE_MOVE | TEvent.EV_MOUSE_AUTO));
            focusItemNum(newItem);
            drawView();
            if (event.mouse.isDouble && (range > focused)) {
                selectItem(focused);
            }
            clearEvent(event);
        } else if (event.what == TEvent.EV_KEYDOWN) {
            int newItem;
            if (event.key.charCode == ' ' && focused < range) {
                selectItem(focused);
                newItem = focused;
            } else {
                switch (KeyCode.ctrlToArrow(event.key.keyCode)) {
                    case KeyCode.KB_UP -> newItem = focused - 1;
                    case KeyCode.KB_DOWN -> newItem = focused + 1;
                    case KeyCode.KB_RIGHT -> {
                        if (numCols > 1) {
                            newItem = focused + size.y;
                        } else {
                            return;
                        }
                    }
                    case KeyCode.KB_LEFT -> {
                        if (numCols > 1) {
                            newItem = focused - size.y;
                        } else {
                            return;
                        }
                    }
                    case KeyCode.KB_PAGE_DOWN -> newItem = focused + size.y * numCols;
                    case KeyCode.KB_PAGE_UP -> newItem = focused - size.y * numCols;
                    case KeyCode.KB_HOME -> newItem = topItem;
                    case KeyCode.KB_END -> newItem = topItem + (size.y * numCols) - 1;
                    case KeyCode.KB_CTRL_PAGE_DOWN -> newItem = range - 1;
                    case KeyCode.KB_CTRL_PAGE_UP -> newItem = 0;
                    default -> {
                        return;
                    }
                }
            }
            focusItemNum(newItem);
            drawView();
            clearEvent(event);
        } else if (event.what == TEvent.EV_BROADCAST) {
            if ((options & Options.OF_SELECTABLE) != 0) {
                if (event.msg.command == Command.CM_SCROLLBAR_CLICKED &&
                        (event.msg.infoPtr == hScrollBar || event.msg.infoPtr == vScrollBar)) {
                    select();
                } else if (event.msg.command == Command.CM_SCROLLBAR_CHANGED) {
                    if (event.msg.infoPtr == vScrollBar) {
                        focusItemNum(vScrollBar.value);
                        drawView();
                    } else if (event.msg.infoPtr == hScrollBar) {
                        drawView();
                    }
                }
            }
        }
    }

    /** Broadcasts a list item selection message to the owner. */
    protected void selectItem(int item) {
        TView.message(getOwner(), TEvent.EV_BROADCAST, Command.CM_LIST_ITEM_SELECTED, this);
    }

    /** Sets the total number of items and updates the vertical scroll bar. */
    public void setRange(int aRange) {
        range = aRange;
        if (vScrollBar != null) {
            if (focused > aRange) {
                focused = 0;
            }
            vScrollBar.setParams(focused, 0, aRange - 1, vScrollBar.pgStep, vScrollBar.arStep);
        }
    }

    @Override
    public void setState(int aState, boolean enable) {
        logger.trace("{} TListViewer@setState(state={}, enable={})", getLogName(), aState, enable);
        super.setState(aState, enable);
        if ((aState & (State.SF_SELECTED | State.SF_ACTIVE | State.SF_VISIBLE)) != 0) {
            showSBar(hScrollBar);
            showSBar(vScrollBar);
            drawView();
        }
    }

    private void showSBar(TScrollBar sBar) {
        if (sBar != null) {
            if (getState(State.SF_ACTIVE) && getState(State.SF_VISIBLE)) {
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
            stream.writeInt(numCols);
            stream.writeInt(topItem);
            stream.writeInt(focused);
            stream.writeInt(range);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

