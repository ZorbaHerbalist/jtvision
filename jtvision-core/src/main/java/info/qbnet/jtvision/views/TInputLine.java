package info.qbnet.jtvision.views;

import com.fasterxml.jackson.databind.node.ObjectNode;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TInputLine extends TView {

    public static final int CLASS_ID = 11;

    /**
     * Palette roles for {@link TInputLine}.
     */
    public enum InputLineColor implements PaletteRole {
        /** Normal state. */
        NORMAL(0x13),
        /** Active/focused state. */
        ACTIVE(0x13),
        /** Selected text. */
        SELECTED_TEXT(0x14),
        /** Scroll arrows. */
        ARROWS(0x15);

        private final byte defaultValue;

        InputLineColor(int defaultValue) {
            this.defaultValue = PaletteRole.toByte(defaultValue);
        }

        @Override
        public byte defaultValue() {
            return defaultValue;
        }
    }

    public static void registerType() {
        TStream.registerType(CLASS_ID, TInputLine::new);
        JsonViewStore.registerType(TInputLine.class, TInputLine::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    protected int maxLen;
    protected StringBuilder data;

    protected int firstPos = 0;
    protected int curPos = 0;

    protected int selStart = 0;
    protected int selEnd = 0;

    public static final PaletteDescriptor<InputLineColor> INPUT_LINE_PALETTE =
            PaletteDescriptor.register("inputLine", InputLineColor.class);

    public TInputLine(TRect bounds, int maxLen) {
        super(bounds);
        this.state |= State.SF_CURSOR_VIS;
        this.options |= Options.OF_SELECTABLE + Options.OF_FIRST_CLICK;
        this.maxLen = maxLen;
        this.data = new StringBuilder(maxLen);
    }

    public TInputLine(TStream stream) {
        super(stream);
        try {
            maxLen = stream.readInt();
            String text = stream.readString();
            data = new StringBuilder(maxLen);
            if (text != null) {
                data.append(text);
            }
            firstPos = stream.readInt();
            curPos = stream.readInt();
            selStart = stream.readInt();
            selEnd = stream.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TInputLine(ObjectNode node) {
        super(node);
        maxLen = JsonUtil.getInt(node, "maxLen", 0);
        data = new StringBuilder(Math.max(maxLen, 0));
        String text = JsonUtil.getString(node, "text");
        if (text != null) {
            data.append(text);
        }
        firstPos = JsonUtil.getInt(node, "firstPos", 0);
        curPos = JsonUtil.getInt(node, "curPos", 0);
        selStart = JsonUtil.getInt(node, "selStart", 0);
        selEnd = JsonUtil.getInt(node, "selEnd", 0);
    }

    /** Returns whether the line can be scrolled further by {@code delta}. */
    private boolean canScroll(int delta) {
        if (delta < 0) {
            return firstPos > 0;
        } else if (delta > 0) {
            return data.length() - firstPos + 2 > size.x;
        } else {
            return false;
        }
    }

    @Override
    public int dataSize() {
        return maxLen + 2;
    }

    @Override
    public void draw() {
        TDrawBuffer buf = new TDrawBuffer();
        short color = (state & State.SF_FOCUSED) != 0 ? getColor(InputLineColor.ACTIVE)
                : getColor(InputLineColor.NORMAL);

        buf.moveChar(0, ' ', color, size.x);
        int len = Math.min(size.x - 2, data.length() - firstPos);
        if (len > 0) {
            buf.moveStr(1, data.substring(firstPos, firstPos + len), color);
        }
        if (canScroll(1)) {
            buf.moveChar(size.x - 1, (char) 0x10, getColor(InputLineColor.ARROWS), 1);
        }
        if ((state & State.SF_FOCUSED) != 0) {
            if (canScroll(-1)) {
                buf.moveChar(0, (char) 0x11, getColor(InputLineColor.ARROWS), 1);
            }
            int l = selStart - firstPos;
            int r = selEnd - firstPos;
            if (l < 0) l = 0;
            if (r > size.x - 2) r = size.x - 2;
            if (l < r) {
                buf.moveChar(l + 1, (char) 0x00, getColor(InputLineColor.SELECTED_TEXT), r - l);
            }
        }
        writeLine(0, 0, size.x, size.y, buf.buffer);
        setCursor(curPos - firstPos + 1, 0);
    }

    @Override
    public void getData(ByteBuffer dst) {
        int required = dataSize();
        if (dst.remaining() < required) {
            throw new java.nio.BufferOverflowException();
        }

        byte[] bytes = data.toString().getBytes(StandardCharsets.UTF_8);
        int len = Math.min(bytes.length, maxLen);
        dst.put((byte) (len & 0xFF));
        dst.put((byte) ((len >>> 8) & 0xFF));
        dst.put(bytes, 0, len);
        for (int i = len; i < maxLen; i++) {
            dst.put((byte) 0);
        }
    }

    @Override
    public TPalette getPalette() {
        return INPUT_LINE_PALETTE.palette();
    }

    private void deleteSelect() {
        if (selStart != selEnd) {
            data.delete(selStart, selEnd);
            curPos = selStart;
        }
    }

    private void adjustSelectBlock(int anchor) {
        if (curPos < anchor) {
            selStart = curPos;
            selEnd = anchor;
        } else {
            selStart = anchor;
            selEnd = curPos;
        }
    }

    private int mouseDelta(TEvent event) {
        TPoint mouse = new TPoint();
        makeLocal(event.mouse.where, mouse);
        if (mouse.x <= 0) return -1;
        if (mouse.x >= size.x - 1) return 1;
        return 0;
    }

    private int mousePos(TEvent event) {
        TPoint mouse = new TPoint();
        makeLocal(event.mouse.where, mouse);
        int x = mouse.x;
        if (x < 1) x = 1;
        int pos = x + firstPos - 1;
        if (pos < 0) pos = 0;
        if (pos > data.length()) pos = data.length();
        return pos;
    }

    private boolean isPadKey(int keyCode) {
        return keyCode == KeyCode.KB_SHIFT_LEFT || keyCode == KeyCode.KB_SHIFT_RIGHT
                || keyCode == KeyCode.KB_SHIFT_HOME || keyCode == KeyCode.KB_SHIFT_END;
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if ((state & State.SF_SELECTED) == 0) {
            return;
        }

        switch (event.what) {
            case TEvent.EV_MOUSE_DOWN -> {
                int delta = mouseDelta(event);
                if (canScroll(delta)) {
                    do {
                        if (canScroll(delta)) {
                            firstPos += delta;
                            drawView();
                        }
                    } while (mouseEvent(event, TEvent.EV_MOUSE_AUTO));
                } else if (event.mouse.isDouble) {
                    selectAll(true);
                } else {
                    int anchor = mousePos(event);
                    do {
                        if (event.what == TEvent.EV_MOUSE_AUTO) {
                            delta = mouseDelta(event);
                            if (canScroll(delta)) {
                                firstPos += delta;
                            }
                        }
                        curPos = mousePos(event);
                        adjustSelectBlock(anchor);
                        drawView();
                    } while (mouseEvent(event, TEvent.EV_MOUSE_MOVE | TEvent.EV_MOUSE_AUTO));
                }
                clearEvent(event);
            }
            case TEvent.EV_KEYDOWN -> {
                int anchor = 0;
                boolean extendBlock = false;
                if (isPadKey(event.key.keyCode)) {
                    event.key.keyCode &= 0x00FF;
                    anchor = (curPos == selEnd) ? selStart : selEnd;
                    extendBlock = true;
                }
                switch (event.key.keyCode) {
                    case KeyCode.KB_LEFT -> {
                        if (curPos > 0) {
                            curPos--;
                        }
                    }
                    case KeyCode.KB_RIGHT -> {
                        if (curPos < data.length()) {
                            curPos++;
                        }
                    }
                    case KeyCode.KB_HOME -> curPos = 0;
                    case KeyCode.KB_END -> curPos = data.length();
                    case KeyCode.KB_BACK -> {
                        if (curPos > 0) {
                            data.deleteCharAt(curPos - 1);
                            curPos--;
                            if (firstPos > 0) {
                                firstPos--;
                            }
                        }
                    }
                    case KeyCode.KB_DEL -> {
                        if (selStart == selEnd) {
                            if (curPos < data.length()) {
                                selStart = curPos;
                                selEnd = curPos + 1;
                            }
                        }
                        deleteSelect();
                    }
                    case KeyCode.KB_INS -> setState(State.SF_CURSOR_INS,
                            (state & State.SF_CURSOR_INS) == 0);
                    default -> {
                        char ch = event.key.charCode;
                        if (ch >= ' ' && ch != 0) {
                            if ((state & State.SF_CURSOR_INS) != 0 && curPos < data.length()) {
                                data.deleteCharAt(curPos);
                            } else {
                                deleteSelect();
                            }
                            if (data.length() < maxLen) {
                                if (firstPos > curPos) {
                                    firstPos = curPos;
                                }
                                data.insert(curPos, ch);
                                curPos++;
                            }
                        } else if (ch == 0x19) {
                            data.setLength(0);
                            curPos = 0;
                            firstPos = 0;
                        } else {
                            return;
                        }
                    }
                }
                if (extendBlock) {
                    adjustSelectBlock(anchor);
                } else {
                    selStart = curPos;
                    selEnd = curPos;
                }
                if (firstPos > curPos) {
                    firstPos = curPos;
                }
                int i = curPos - size.x + 2;
                if (firstPos < i) {
                    firstPos = i;
                }
                drawView();
                clearEvent(event);
            }
        }
    }

    public void selectAll(boolean enable) {
        curPos = 0;
        firstPos = 0;
        selStart = 0;
        selEnd = enable ? data.length() : 0;
        drawView();
    }

    @Override
    public void setData(ByteBuffer src) {
        int available = Math.min(dataSize(), src.remaining());
        if (available <= 0) {
            return;
        }

        int start = src.position();
        int fieldEnd = start + available;

        if (available < 2) {
            src.position(fieldEnd);
            data.setLength(0);
            selectAll(true);
            return;
        }

        int lo = src.get() & 0xFF;
        int hi = src.get() & 0xFF;
        int len = (hi << 8) | lo;
        int bytesRemaining = fieldEnd - src.position();

        int copyLen = Math.min(len, Math.min(maxLen, bytesRemaining));
        byte[] bytes = new byte[copyLen];
        src.get(bytes);
        src.position(fieldEnd);

        data.setLength(0);
        data.append(new String(bytes, StandardCharsets.UTF_8));
        selectAll(true);
    }

    @Override
    public void setState(int state, boolean enable) {
        super.setState(state, enable);
        if (state == State.SF_SELECTED || (state == State.SF_ACTIVE && (this.state & State.SF_SELECTED) != 0)) {
            selectAll(enable);
        } else if (state == State.SF_FOCUSED) {
            drawView();
        }
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            stream.writeInt(maxLen);
            stream.writeString(data.toString());
            stream.writeInt(firstPos);
            stream.writeInt(curPos);
            stream.writeInt(selStart);
            stream.writeInt(selEnd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void storeJson(ObjectNode node) {
        super.storeJson(node);
        node.put("maxLen", maxLen);
        node.put("text", data.toString());
        node.put("firstPos", firstPos);
        node.put("curPos", curPos);
        node.put("selStart", selStart);
        node.put("selEnd", selEnd);
    }

}
