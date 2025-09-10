package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple list box displaying a collection of strings.
 * <p>
 * The widget is backed by a {@link List} of strings.  The list and the
 * current selection are transferred through {@link #getData(ByteBuffer)} and
 * {@link #setData(ByteBuffer)} allowing dialogs to exchange their state with
 * the application similar to Turbo Vision's {@code TListBox}.
 * </p>
 */
public class TListBox extends TListViewer {

    public static final int CLASS_ID = 16;

    /** Registers this class for stream persistence. */
    public static void registerType() {
        TStream.registerType(CLASS_ID, TListBox::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    /** Underlying list of item strings. */
    protected List<String> list = new ArrayList<>();

    public TListBox(TRect bounds, int numCols, TScrollBar vScrollBar) {
        super(bounds, numCols, null, vScrollBar);
    }

    public TListBox(TStream stream) {
        super(stream);
        try {
            int count = stream.readInt();
            list = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                list.add(stream.readString());
            }
            setRange(list.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int dataSize() {
        int size = 4; // item count
        for (String s : list) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            size += 2 + bytes.length; // length prefix + data
        }
        size += 4; // selection index
        return size;
    }

    @Override
    public void getData(ByteBuffer dst) {
        dst.putInt(list.size());
        for (String s : list) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            dst.putShort((short) bytes.length);
            dst.put(bytes);
        }
        dst.putInt(focused);
    }

    @Override
    public void setData(ByteBuffer src) {
        int count = src.getInt();
        List<String> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int len = Short.toUnsignedInt(src.getShort());
            byte[] bytes = new byte[len];
            src.get(bytes);
            items.add(new String(bytes, StandardCharsets.UTF_8));
        }
        int sel = src.getInt();
        newList(items);
        focusItem(sel);
        drawView();
    }

    @Override
    protected String getText(int item, int maxLen) {
        if (item >= 0 && item < list.size()) {
            return list.get(item);
        }
        return "";
    }

    /** Replaces the current list with {@code aList} and redraws the view. */
    public void newList(List<String> aList) {
        if (aList == null) {
            list = new ArrayList<>();
        } else {
            list = new ArrayList<>(aList);
        }
        setRange(list.size());
        if (range > 0) {
            focusItem(0);
        }
        drawView();
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            stream.writeInt(list.size());
            for (String s : list) {
                stream.writeString(s);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

