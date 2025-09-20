package info.qbnet.jtvision.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaletteFactoryTest {

    private enum SampleRole implements PaletteRole {
        PRIMARY,
        SECONDARY
    }

    @Test
    void shouldLoadPaletteFromJsonResource() {
        String paletteName = "testPaletteValid";
        PaletteFactory.register(paletteName, SampleRole.class);

        TPalette palette = PaletteFactory.get(paletteName);

        assertEquals(PaletteRole.toByte(0x11), palette.get(SampleRole.PRIMARY));
        assertEquals(PaletteRole.toByte(0x22), palette.get(SampleRole.SECONDARY));
    }

    @Test
    void shouldIgnoreUnknownKeys() {
        String paletteName = "testPaletteUnknown";
        PaletteFactory.register(paletteName, SampleRole.class);

        TPalette palette = PaletteFactory.get(paletteName);

        assertEquals(PaletteRole.toByte(0x55), palette.get(SampleRole.PRIMARY));
        assertEquals(PaletteRole.toByte(0x33), palette.get(SampleRole.SECONDARY));
    }

    @Test
    void shouldFailWhenValuesInvalid() {
        assertThrows(IllegalStateException.class,
                () -> PaletteFactory.register("testPaletteInvalid", SampleRole.class));
    }

    @Test
    void shouldFailWhenJsonMalformed() {
        assertThrows(IllegalStateException.class,
                () -> PaletteFactory.register("testPaletteMalformed", SampleRole.class));
    }

    @Test
    void shouldFailWhenResourceMissing() {
        assertThrows(IllegalStateException.class,
                () -> PaletteFactory.register("nonExistingPalette", SampleRole.class));
    }
}
