package info.qbnet.jtvision.core.objects;

import info.qbnet.jtvision.core.views.TView;
import info.qbnet.jtvision.core.objects.TRect;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TStreamTest {

    static class SampleView extends TView {
        static final int CLASS_ID = 100;
        static { TStream.registerType(CLASS_ID, SampleView::new); }
        int value;
        SampleView() { super(new TRect(0, 0, 1, 1)); }
        public SampleView(TStream stream) {
            super(stream);
            try {
                value = stream.readInt();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public int getClassId() {
            return CLASS_ID;
        }
        @Override
        public void store(TStream stream) {
            super.store(stream);
            try {
                stream.writeInt(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    void readWritePrimitives() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TStream ts = new TStream(out);
        ts.writeShort((short) 0x1234);
        ts.writeInt(0x12345678);
        ts.writeBytes(new byte[]{1,2,3});

        byte[] data = out.toByteArray();
        TStream in = new TStream(new ByteArrayInputStream(data));
        assertEquals(0x1234, Short.toUnsignedInt(in.readShort()));
        assertEquals(0x12345678, in.readInt());
        assertArrayEquals(new byte[]{1,2,3}, in.readBytes(3));
    }

    @Test
    void storeAndLoadView() throws Exception {
        SampleView view = new SampleView();
        view.value = 0xCAFEBABE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TStream outStream = new TStream(out);
        outStream.storeView(view);

        byte[] data = out.toByteArray();
        TStream inStream = new TStream(new ByteArrayInputStream(data));
        TView loaded = inStream.loadView();
        assertTrue(loaded instanceof SampleView);
        assertEquals(view.value, ((SampleView) loaded).value);
    }
}
