package info.qbnet.jtvision.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaletteFactoryTest {

    private enum SampleRole implements PaletteRole {
        PRIMARY(0x10),
        SECONDARY(0x20);

        private final byte defaultValue;

        SampleRole(int defaultValue) {
            this.defaultValue = PaletteRole.toByte(defaultValue);
        }

        @Override
        public byte defaultValue() {
            return defaultValue;
        }
    }

    private enum ExplicitIndexRole implements PaletteRole {
        PRIMARY(5, 0x10),
        SECONDARY(6, 0x20);

        private final int index;
        private final byte defaultValue;

        ExplicitIndexRole(int index, int defaultValue) {
            this.index = index;
            this.defaultValue = PaletteRole.toByte(defaultValue);
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public byte defaultValue() {
            return defaultValue;
        }
    }

    @Test
    void shouldApplyOverridesFromJsonResource() {
        String paletteName = "testPaletteValid";
        PaletteFactory.registerDefaults(paletteName, SampleRole.class);

        TPalette palette = PaletteFactory.get(paletteName);

        assertEquals(PaletteRole.toByte(0x11), palette.get(SampleRole.PRIMARY));
        assertEquals(PaletteRole.toByte(0x22), palette.get(SampleRole.SECONDARY));
    }

    @Test
    void shouldIgnoreUnknownKeys() {
        String paletteName = "testPaletteUnknown";
        PaletteFactory.registerDefaults(paletteName, SampleRole.class);

        TPalette palette = PaletteFactory.get(paletteName);

        assertEquals(SampleRole.PRIMARY.defaultValue(), palette.get(SampleRole.PRIMARY));
        assertEquals(PaletteRole.toByte(0x33), palette.get(SampleRole.SECONDARY));
    }

    @Test
    void shouldFallbackToDefaultOnInvalidValues() {
        String paletteName = "testPaletteInvalid";
        PaletteFactory.registerDefaults(paletteName, SampleRole.class);

        TPalette palette = PaletteFactory.get(paletteName);

        assertEquals(SampleRole.PRIMARY.defaultValue(), palette.get(SampleRole.PRIMARY));
        assertEquals(PaletteRole.toByte(0x44), palette.get(SampleRole.SECONDARY));
    }

    @Test
    void shouldFallbackToDefaultsWhenJsonMalformed() {
        String paletteName = "testPaletteMalformed";
        PaletteFactory.registerDefaults(paletteName, SampleRole.class);

        TPalette palette = PaletteFactory.get(paletteName);

        assertEquals(SampleRole.PRIMARY.defaultValue(), palette.get(SampleRole.PRIMARY));
        assertEquals(SampleRole.SECONDARY.defaultValue(), palette.get(SampleRole.SECONDARY));
    }

    @Test
    void shouldRegisterAutoIndexedPalette() {
        String paletteName = "testPaletteAuto";
        PaletteFactory.registerAutoIndexed(paletteName, SampleRole.class);

        TPalette palette = PaletteFactory.get(paletteName);

        assertEquals(SampleRole.PRIMARY.defaultValue(), palette.get(SampleRole.PRIMARY));
        assertEquals(SampleRole.SECONDARY.defaultValue(), palette.get(SampleRole.SECONDARY));
    }

    @Test
    void shouldRejectAutoIndexedWhenIndicesAreExplicit() {
        assertThrows(IllegalArgumentException.class,
                () -> PaletteFactory.registerAutoIndexed("testPaletteExplicit", ExplicitIndexRole.class));
    }
}
