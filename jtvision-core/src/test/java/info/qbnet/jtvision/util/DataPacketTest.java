package info.qbnet.jtvision.util;

import org.junit.jupiter.api.Test;

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
}
