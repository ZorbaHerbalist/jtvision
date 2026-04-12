package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.PaletteRole;
import info.qbnet.jtvision.util.TPalette;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.support.TestGroup;
import info.qbnet.jtvision.views.support.TestableTView;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TViewPaletteUnsignedMappingTest {

    private enum LeafRole implements PaletteRole {
        SIX(6);

        private final int index;

        LeafRole(int index) {
            this.index = index;
        }

        @Override
        public int index() {
            return index;
        }
    }

    private enum RootRole implements PaletteRole {
        INDEX_165(165);

        private final int index;

        RootRole(int index) {
            this.index = index;
        }

        @Override
        public int index() {
            return index;
        }
    }

    @Test
    void mapsPaletteBytesAsUnsignedAcrossOwnerChain() {
        EnumMap<LeafRole, Byte> leafMap = new EnumMap<>(LeafRole.class);
        leafMap.put(LeafRole.SIX, (byte) 0xA5); // 165 unsigned
        TPalette leafPalette = new TPalette(leafMap);

        EnumMap<RootRole, Byte> rootMap = new EnumMap<>(RootRole.class);
        rootMap.put(RootRole.INDEX_165, (byte) 0x2A);
        TPalette rootPalette = new TPalette(rootMap);

        TestGroup root = new TestGroup(new TRect(0, 0, 20, 10), rootPalette);
        TestableTView leaf = new TestableTView(new TRect(0, 0, 5, 1), leafPalette);
        root.insert(leaf);

        int resolved = leaf.getColor((short) 6) & 0xFF;
        assertEquals(0x2A, resolved);
    }
}
