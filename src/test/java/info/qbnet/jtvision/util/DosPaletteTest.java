package info.qbnet.jtvision.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DosPaletteTest {
    @Test
    void decodesForegroundAndBackground() {
        int attr = 0x1E; // fg=14 (YELLOW), bg=1 (BLUE)
        assertEquals(DosColor.YELLOW.toAwt(), DosPalette.getForeground(attr));
        assertEquals(DosColor.BLUE.toAwt(), DosPalette.getBackground(attr));
    }

    @Test
    void decodesBackgroundIntensity() {
        int attr = 0xF0; // fg=0, bg=7 with intensity -> WHITE
        assertEquals(DosColor.WHITE.toAwt(), DosPalette.getBackground(attr));
    }
}

