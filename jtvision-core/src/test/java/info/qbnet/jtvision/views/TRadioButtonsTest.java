package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TRadioButtonsTest {

    @Test
    void constructorLoadsStateFromStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TStream ts = new TStream(out);

        // Base TView fields (12 integers)
        for (int i = 0; i < 12; i++) {
            ts.writeInt(0);
        }

        // TCluster specific fields
        ts.writeInt(2);            // value
        ts.writeInt(1);            // sel
        ts.writeInt(0xAAAA);      // enableMask
        ts.writeInt(3);            // number of strings
        ts.writeString("One");
        ts.writeString("Two");
        ts.writeString("Three");

        TStream in = new TStream(new ByteArrayInputStream(out.toByteArray()));
        TRadioButtons buttons = new TRadioButtons(in);

        assertEquals(2, buttons.value);
        assertEquals(1, buttons.sel);
        assertEquals(0xAAAA, buttons.enableMask);
        assertEquals(List.of("One", "Two", "Three"), buttons.strings);
    }

    @Test
    void storeWritesStateToStream() throws Exception {
        TRadioButtons buttons = new TRadioButtons(new TRect(0, 0, 10, 5), List.of("One", "Two", "Three"));
        buttons.value = 2;
        buttons.sel = 1;
        buttons.enableMask = 0x5555AAAA;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TStream ts = new TStream(out);
        buttons.store(ts);

        TStream in = new TStream(new ByteArrayInputStream(out.toByteArray()));
        // Skip base TView fields
        for (int i = 0; i < 12; i++) {
            in.readInt();
        }
        assertEquals(2, in.readInt()); // value
        assertEquals(1, in.readInt()); // sel
        assertEquals(0x5555AAAA, in.readInt()); // enableMask
        assertEquals(3, in.readInt()); // string count
        assertEquals("One", in.readString());
        assertEquals("Two", in.readString());
        assertEquals("Three", in.readString());
    }
}

