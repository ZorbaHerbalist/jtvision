package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TDrawBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TInputLineDrawTest {

    @Test
    void selectedTextIsHighlightedWithoutChangingCharacters() {
        TInputLine line = new TInputLine(new TRect(0, 0, 6, 1), 10);
        line.data.append("Hello");

        TDrawBuffer buf = new TDrawBuffer();
        int normal = 0x11;
        int highlight = 0x22;

        buf.moveChar(0, ' ', normal, 6);
        buf.moveStr(1, line.data.toString(), normal);

        // highlight "el"
        buf.moveChar(2, (char) 0, highlight, 2);

        assertEquals('e', buf.buffer[2] & 0xFF);
        assertEquals(highlight, (buf.buffer[2] >>> 8) & 0xFF);
        assertEquals('l', buf.buffer[3] & 0xFF);
        assertEquals(highlight, (buf.buffer[3] >>> 8) & 0xFF);
    }
}
