package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.objects.TRect;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TInputLineDataTest {

    @Test
    void dataSizeReflectsStringBytes() {
        TInputLine line = new TInputLine(new TRect(0, 0, 1, 1), 20);
        line.data.append("Hello");

        assertEquals("Hello".getBytes(StandardCharsets.UTF_8).length, line.dataSize());
    }

    @Test
    void getDataWritesStringAsBytes() {
        TInputLine line = new TInputLine(new TRect(0, 0, 1, 1), 20);
        line.data.append("Test");

        ByteBuffer dst = ByteBuffer.allocate(line.dataSize());
        line.getData(dst);

        assertArrayEquals("Test".getBytes(StandardCharsets.UTF_8), dst.array());
        assertEquals(line.dataSize(), dst.position());
    }

    @Test
    void setDataReadsBytesIntoString() {
        TInputLine line = new TInputLine(new TRect(0, 0, 1, 1), 20);
        byte[] bytes = "World".getBytes(StandardCharsets.UTF_8);
        ByteBuffer src = ByteBuffer.wrap(bytes);

        line.setData(src);

        assertEquals("World", line.data.toString());
        assertEquals(bytes.length, src.position());
    }
}
