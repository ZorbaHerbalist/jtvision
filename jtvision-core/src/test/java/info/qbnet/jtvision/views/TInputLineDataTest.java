package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.DataPacket;
import info.qbnet.jtvision.util.TRect;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TInputLineDataTest {

    @Test
    void dataSizeIncludesLengthPrefixAndCapacity() {
        TInputLine line = new TInputLine(new TRect(0, 0, 1, 1), 20);

        assertEquals(22, line.dataSize());
    }

    @Test
    void getDataWritesLengthPrefixedStringWithPadding() {
        TInputLine line = new TInputLine(new TRect(0, 0, 1, 1), 10);
        line.data.append("Test");

        ByteBuffer dst = ByteBuffer.allocate(line.dataSize());
        line.getData(dst);

        byte[] bytes = dst.array();
        int len = ((bytes[1] & 0xFF) << 8) | (bytes[0] & 0xFF);
        assertEquals(4, len);
        assertEquals('T', bytes[2]);
        assertEquals('e', bytes[3]);
        assertEquals('s', bytes[4]);
        assertEquals('t', bytes[5]);
        for (int i = 6; i < bytes.length; i++) {
            assertEquals(0, bytes[i]);
        }
        assertEquals(line.dataSize(), dst.position());
    }

    @Test
    void setDataReadsLengthPrefixedString() {
        TInputLine line = new TInputLine(new TRect(0, 0, 1, 1), 20);
        DataPacket packet = new DataPacket(line.dataSize())
                .putStringField("World", line.dataSize())
                .rewind();

        line.setData(packet.getByteBuffer());

        assertEquals("World", line.data.toString());
    }

    @Test
    void setDataSupportsLegacyRawBytes() {
        TInputLine line = new TInputLine(new TRect(0, 0, 1, 1), 20);
        byte[] legacy = "Legacy".getBytes(StandardCharsets.UTF_8);
        ByteBuffer src = ByteBuffer.wrap(legacy);

        line.setData(src);

        assertEquals("Legacy", line.data.toString());
        assertEquals(legacy.length, src.position());
    }
}
