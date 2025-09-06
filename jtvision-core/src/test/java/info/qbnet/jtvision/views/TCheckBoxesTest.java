package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TCheckBoxesTest {

    @Test
    void constructorLoadsStateFromStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TStream ts = new TStream(out);

        // Base TView fields (12 integers)
        for (int i = 0; i < 12; i++) {
            ts.writeInt(0);
        }

        // TCluster specific fields
        ts.writeInt(0xA5A5);     // value (bitmask)
        ts.writeInt(1);          // sel
        ts.writeInt(0xAAAA);     // enableMask
        ts.writeInt(3);          // number of strings
        ts.writeString("One");
        ts.writeString("Two");
        ts.writeString("Three");

        TStream in = new TStream(new ByteArrayInputStream(out.toByteArray()));
        TCheckBoxes boxes = new TCheckBoxes(in);

        assertEquals(0xA5A5, boxes.value);
        assertEquals(1, boxes.sel);
        assertEquals(0xAAAA, boxes.enableMask);
        assertEquals(List.of("One", "Two", "Three"), boxes.strings);
    }

    @Test
    void storeWritesStateToStream() throws Exception {
        TCheckBoxes boxes = new TCheckBoxes(new TRect(0, 0, 10, 5), List.of("One", "Two", "Three"));
        boxes.value = 0x1234;
        boxes.sel = 2;
        boxes.enableMask = 0x5555AAAA;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TStream ts = new TStream(out);
        boxes.store(ts);

        TStream in = new TStream(new ByteArrayInputStream(out.toByteArray()));
        // Skip base TView fields
        for (int i = 0; i < 12; i++) {
            in.readInt();
        }
        assertEquals(0x1234, in.readInt()); // value
        assertEquals(2, in.readInt()); // sel
        assertEquals(0x5555AAAA, in.readInt()); // enableMask
        assertEquals(3, in.readInt()); // string count
        assertEquals("One", in.readString());
        assertEquals("Two", in.readString());
        assertEquals("Three", in.readString());
    }

    @Test
    void pressTogglesMark() {
        TCheckBoxes boxes = new TCheckBoxes(new TRect(0, 0, 10, 5), List.of("One", "Two", "Three"));
        assertFalse(boxes.mark(1));
        boxes.press(1);
        assertTrue(boxes.mark(1));
        assertEquals(1 << 1, boxes.value);
        boxes.press(1);
        assertFalse(boxes.mark(1));
        assertEquals(0, boxes.value);
    }
}

