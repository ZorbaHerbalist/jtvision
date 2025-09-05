package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.TDrawBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TDrawBufferTest {

    @Test
    void moveBufWithAttrZeroPreservesColor() {
        TDrawBuffer buf = new TDrawBuffer();
        buf.moveChar(0, 'A', 0x12, 1);
        short[] src = new short[]{(short) ((0x34 << 8) | 'B')};
        buf.moveBuf(0, src, 0, 1);
        assertEquals('B', buf.buffer[0] & 0xFF);
        assertEquals(0x12, (buf.buffer[0] >>> 8) & 0xFF);
    }

    @Test
    void moveCharWithZeroCharChangesOnlyAttribute() {
        TDrawBuffer buf = new TDrawBuffer();
        buf.moveChar(0, 'X', 0x12, 1);

        buf.moveChar(0, (char) 0, 0x34, 1);

        assertEquals('X', buf.buffer[0] & 0xFF);
        assertEquals(0x34, (buf.buffer[0] >>> 8) & 0xFF);
    }

    @Test
    void moveCharWithZeroAttrChangesOnlyCharacter() {
        TDrawBuffer buf = new TDrawBuffer();
        buf.moveChar(0, 'X', 0x12, 1);

        buf.moveChar(0, 'Y', 0, 1);

        assertEquals('Y', buf.buffer[0] & 0xFF);
        assertEquals(0x12, (buf.buffer[0] >>> 8) & 0xFF);
    }

    @Test
    void moveCharWithNonZeroCharAndAttrOverwritesBoth() {
        TDrawBuffer buf = new TDrawBuffer();
        buf.moveChar(0, 'X', 0x12, 1);

        buf.moveChar(0, 'Y', 0x34, 1);

        assertEquals('Y', buf.buffer[0] & 0xFF);
        assertEquals(0x34, (buf.buffer[0] >>> 8) & 0xFF);
    }
}
