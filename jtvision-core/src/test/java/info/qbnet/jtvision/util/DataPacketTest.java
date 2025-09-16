package info.qbnet.jtvision.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DataPacketTest {

    @Test
    void roundtripStringAndShort() {
        DataPacket packet = new DataPacket(32)
                .putString("test")
                .putShort((short) 42)
                .rewind();

        assertEquals("test", packet.getString());
        assertEquals(42, packet.getShort());
    }

    @Test
    void supportsInts() {
        DataPacket packet = new DataPacket(16)
                .putInt(123456)
                .rewind();

        assertEquals(123456, packet.getInt());
    }

    @Test
    void shortsUseLittleEndian() {
        DataPacket packet = new DataPacket(4)
                .putShort((short) 0x1234);

        byte[] bytes = packet.toByteArray();
        assertArrayEquals(new byte[]{0x34, 0x12}, Arrays.copyOf(bytes, 2));
    }

    @Test
    void stringFieldRoundtrip() {
        DataPacket packet = new DataPacket(8)
                .putStringField("Hi", 6)
                .rewind();

        assertEquals("Hi", packet.getStringField(6));
    }
}
