package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.PaletteFactory;
import info.qbnet.jtvision.util.TStatusDef;
import info.qbnet.jtvision.util.TStatusItem;
import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;
import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.TPalette;
import info.qbnet.jtvision.util.CString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public class TStatusLine extends TView {

    public static final int CLASS_ID = 42;

    public static void registerType() {
        TStream.registerType(CLASS_ID, TStatusLine::new);
    }

    public static StatusDefBuilder statusLine() {
        return new StatusDefBuilder();
    }

    public static ItemsBuilder items() {
        return new ItemsBuilder();
    }

    public static final class StatusDefBuilder {
        private final List<TStatusDef> defs = new ArrayList<>();

        public StatusDefBuilder def(int min, int max, Consumer<ItemsBuilder> consumer) {
            ItemsBuilder builder = new ItemsBuilder();
            consumer.accept(builder);
            defs.add(new TStatusDef(min, max, builder.build(), null));
            return this;
        }

        public TStatusDef build() {
            return build(null);
        }

        public TStatusDef build(TStatusDef tail) {
            TStatusDef next = tail;
            for (int i = defs.size() - 1; i >= 0; i--) {
                TStatusDef d = defs.get(i);
                next = new TStatusDef(d.min(), d.max(), d.items(), next);
            }
            return next;
        }
    }

    public static final class ItemsBuilder {
        private final List<TStatusItem> items = new ArrayList<>();
        private TStatusItem tail;

        public ItemsBuilder item(String text, int keyCode, int command) {
            items.add(new TStatusItem(text, keyCode, command, null));
            return this;
        }

        public ItemsBuilder chain(TStatusItem tail) {
            this.tail = tail;
            return this;
        }

        public TStatusItem build() {
            return build(tail);
        }

        public TStatusItem build(TStatusItem tail) {
            TStatusItem next = tail;
            for (int i = items.size() - 1; i >= 0; i--) {
                TStatusItem t = items.get(i);
                next = new TStatusItem(t.text(), t.keyCode(), t.command(), next);
            }
            return next;
        }
    }

    private TStatusDef defs;
    private TStatusItem items;

    public static final TPalette C_STATUS_LINE;

    static {
        PaletteFactory.registerDefaults("statusLine", TMenuView.MenuColor.class,
                "\\x02\\x03\\x04\\x05\\x06\\x07");
        C_STATUS_LINE = PaletteFactory.get("statusLine");
    }

    public TStatusLine(TRect bounds, TStatusDef defs) {
        super(bounds);
        this.options |= Options.OF_PRE_PROCESS;
        this.eventMask |= TEvent.EV_BROADCAST;
        setGrowModes(EnumSet.of(GrowMode.GF_GROW_LO_Y, GrowMode.GF_GROW_HI_X, GrowMode.GF_GROW_HI_Y));
        this.defs = defs;
        findItems();

        logger.debug("{} TStatusLine@TStatusLine(bounds={}, defs={})", getLogName(), bounds, defs);
    }

    public TStatusLine(TStream stream) {
        super(stream);
        this.options |= Options.OF_PRE_PROCESS;
        this.eventMask |= TEvent.EV_BROADCAST;
        setGrowModes(EnumSet.of(GrowMode.GF_GROW_LO_Y, GrowMode.GF_GROW_HI_X, GrowMode.GF_GROW_HI_Y));
        try {
            this.defs = readDefs(stream);
            findItems();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void draw() {
        logger.trace("{} TStatusLine@draw()", getLogName());

        drawSelect(null);
    }

    private void drawSelect(TStatusItem selected) {
        TDrawBuffer buf = new TDrawBuffer();

        short cNormal = getColor((short) 0x0301);
        short cSelect = getColor((short) 0x0604);
        short cNormDisabled = getColor((short) 0x0202);
        short cSelDisabled = getColor((short) 0x0505);

        buf.moveChar(0, ' ', cNormal, size.x);
        TStatusItem t = items;
        int i = 0;
        while (t != null) {
            logger.trace("{} TStatusLine@drawSelect() item {}", getLogName(), t);
            if (t.text() != null) {
                int l = CString.cStrLen(t.text());
                if (i + l < size.x) {
                    short color;
                    if (commandEnabled(t.command())) {
                        if (t == selected) {
                            color = cSelect;
                        } else {
                            color = cNormal;
                        }
                    } else {
                        if (t == selected) {
                            color = cSelDisabled;
                        } else {
                            color = cNormDisabled;
                        }
                    }
                    buf.moveChar(i, ' ' , (char) color, 1);
                    buf.moveCStr(i + 1, t.text(), color);
                    buf.moveChar(i + l + 1, ' ', (char) color, 1);
                }
                i = i + l + 2;
            }
            t = t.next();
        }

        if (i < size.x - 2) {
            String hintBuf = hint(helpCtx);
            if (hintBuf != null && hintBuf.length() > 0) {
                buf.moveChar(i, (char) 179, cNormal, 1);
                i += 2;
                if (i + hintBuf.length() > size.x) {
                    hintBuf = hintBuf.substring(0, size.x - i);
                }
                buf.moveCStr(i, hintBuf, (byte) cNormal);
            }
        }

        writeLine(0, 0, size.x, 1, buf.buffer);
    }

    private void findItems() {
        TStatusDef p = defs;
        while (p != null && (helpCtx < p.min() || helpCtx > p.max())) {
            p = p.next();
        }
        if (p == null) {
            items = null;
        } else {
            items = p.items();
        }
    }

    private TStatusItem itemMouseIsIn(TPoint mouse) {
        if (mouse.y != 0) {
            return null;
        }
        int i = 0;
        TStatusItem t = items;
        while (t != null) {
            if (t.text() != null) {
                int k = i + CString.cStrLen(t.text()) + 2;
                if (mouse.x >= i && mouse.x < k) {
                    return t;
                }
                i = k;
            }
            t = t.next();
        }
        return null;
    }

    @Override
    public TPalette getPalette() {
        return C_STATUS_LINE;
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        switch (event.what) {
            case TEvent.EV_MOUSE_DOWN: {
                TStatusItem t = null;
                TPoint mouse = new TPoint();
                while (true) {
                    makeLocal(event.mouse.where, mouse);
                    TStatusItem in = itemMouseIsIn(mouse);
                    if (t != in) {
                        t = in;
                        drawSelect(t);
                    }
                    TProgram.getMouseEvent(event);
                    if (event.what != TEvent.EV_MOUSE_MOVE) {
                        break;
                    }
                }
                if (t != null && commandEnabled(t.command())) {
                    event.what = TEvent.EV_COMMAND;
                    event.msg.command = t.command();
                    event.msg.infoPtr = null;
                    putEvent(event);
                }
                clearEvent(event);
                drawView();
                break;
            }
            case TEvent.EV_KEYDOWN: {
                TStatusItem t = items;
                while (t != null) {
                    if (event.key.keyCode == t.keyCode() && commandEnabled(t.command())) {
                        event.what = TEvent.EV_COMMAND;
                        event.msg.command = t.command();
                        event.msg.infoPtr = null;
                        return;
                    }
                    t = t.next();
                }
                break;
            }
            case TEvent.EV_BROADCAST:
                if (event.msg.command == Command.CM_COMMAND_SET_CHANGED) {
                    drawView();
                }
                break;
        }
    }

    public String hint(int helpCtx) {
        return "";
    }

    /**
     * Updates the status line based on the help context of the top-most view.
     *
     * <p>If the help context has changed since the last update, this method
     * refreshes the status line's items and redraws the view.</p>
     */
    public void update() {
        TView p = topView();
        int h;
        if (p != null) {
            h = p.getHelpCtx();
        } else {
            h = HelpContext.HC_NO_CONTEXT;
        }
        if (h != helpCtx) {
            helpCtx = h;
            findItems();
            drawView();
        }
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            writeDefs(stream, defs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeDefs(TStream stream, TStatusDef def) throws IOException {
        int count = 0;
        for (TStatusDef d = def; d != null; d = d.next()) {
            count++;
        }
        stream.writeInt(count);
        for (TStatusDef d = def; d != null; d = d.next()) {
            stream.writeInt(d.min());
            stream.writeInt(d.max());
            writeItems(stream, d.items());
        }
    }

    private static TStatusDef readDefs(TStream stream) throws IOException {
        int count = stream.readInt();
        TStatusDef next = null;
        List<TStatusDef> defs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int min = stream.readInt();
            int max = stream.readInt();
            TStatusItem items = readItems(stream);
            defs.add(new TStatusDef(min, max, items, null));
        }
        for (int i = count - 1; i >= 0; i--) {
            TStatusDef d = defs.get(i);
            next = new TStatusDef(d.min(), d.max(), d.items(), next);
        }
        return next;
    }

    private static void writeItems(TStream stream, TStatusItem item) throws IOException {
        int count = 0;
        for (TStatusItem t = item; t != null; t = t.next()) {
            count++;
        }
        stream.writeInt(count);
        for (TStatusItem t = item; t != null; t = t.next()) {
            stream.writeString(t.text());
            stream.writeInt(t.keyCode());
            stream.writeInt(t.command());
        }
    }

    private static TStatusItem readItems(TStream stream) throws IOException {
        int count = stream.readInt();
        List<TStatusItem> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String text = stream.readString();
            int key = stream.readInt();
            int cmd = stream.readInt();
            list.add(new TStatusItem(text, key, cmd, null));
        }
        TStatusItem next = null;
        for (int i = count - 1; i >= 0; i--) {
            TStatusItem t = list.get(i);
            next = new TStatusItem(t.text(), t.keyCode(), t.command(), next);
        }
        return next;
    }

}
