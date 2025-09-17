package info.qbnet.jtvision.util;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.*;

class PaletteDescriptorTest {

    private enum TestRole implements PaletteRole {
        FOREGROUND(1, 0x01),
        HIGHLIGHT(2, 0x02);

        private final int index;
        private final byte defaultValue;

        TestRole(int index, int defaultValue) {
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
    void exposesPaletteAndColorConvenienceMethods() {
        PaletteDescriptor<TestRole> descriptor =
                PaletteDescriptor.register("test.paletteDescriptor.default", TestRole.class);

        TPalette palette = descriptor.palette();
        assertNotNull(palette);
        assertEquals(2, palette.length());

        assertEquals(TestRole.FOREGROUND.defaultValue(), descriptor.color(TestRole.FOREGROUND));
        assertEquals(TestRole.HIGHLIGHT.defaultValue(), descriptor.color(TestRole.HIGHLIGHT));

        short packed = descriptor.colorPair(TestRole.FOREGROUND, TestRole.HIGHLIGHT);
        int expected = (Byte.toUnsignedInt(TestRole.HIGHLIGHT.defaultValue()) << 8)
                | Byte.toUnsignedInt(TestRole.FOREGROUND.defaultValue());
        assertEquals((short) expected, packed);
    }

    @Test
    void supportsTemporaryPaletteOverrides() {
        PaletteDescriptor<TestRole> descriptor =
                PaletteDescriptor.register("test.paletteDescriptor.override", TestRole.class);

        EnumMap<TestRole, Byte> overrideData = new EnumMap<>(TestRole.class);
        overrideData.put(TestRole.FOREGROUND, PaletteRole.toByte(0x10));
        overrideData.put(TestRole.HIGHLIGHT, PaletteRole.toByte(0x20));
        TPalette override = new TPalette(overrideData);

        descriptor.overridePalette(override);
        assertTrue(descriptor.hasOverride());
        assertSame(override, descriptor.palette());
        assertEquals(override.get(TestRole.FOREGROUND), descriptor.color(TestRole.FOREGROUND));

        short packed = descriptor.colorPair(TestRole.FOREGROUND, TestRole.HIGHLIGHT);
        int expected = (Byte.toUnsignedInt(override.get(TestRole.HIGHLIGHT)) << 8)
                | Byte.toUnsignedInt(override.get(TestRole.FOREGROUND));
        assertEquals((short) expected, packed);

        descriptor.clearOverride();
        assertFalse(descriptor.hasOverride());
        assertNotSame(override, descriptor.palette());
    }
}
