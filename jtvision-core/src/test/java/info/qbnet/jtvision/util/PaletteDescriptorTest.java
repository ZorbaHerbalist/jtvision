package info.qbnet.jtvision.util;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.*;

class PaletteDescriptorTest {

    private enum TestRole implements PaletteRole {
        FOREGROUND,
        HIGHLIGHT
    }

    @Test
    void exposesPaletteAndColorConvenienceMethods() {
        PaletteDescriptor<TestRole> descriptor =
                PaletteDescriptor.register("test.paletteDescriptor.default", TestRole.class);

        TPalette palette = descriptor.palette();
        assertNotNull(palette);
        assertEquals(2, palette.length());

        assertEquals(PaletteRole.toByte(0x11), descriptor.color(TestRole.FOREGROUND));
        assertEquals(PaletteRole.toByte(0x22), descriptor.color(TestRole.HIGHLIGHT));

        short packed = descriptor.colorPair(TestRole.FOREGROUND, TestRole.HIGHLIGHT);
        int expected = (Byte.toUnsignedInt(PaletteRole.toByte(0x22)) << 8)
                | Byte.toUnsignedInt(PaletteRole.toByte(0x11));
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
