package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.core.objects.TPoint;
import info.qbnet.jtvision.core.objects.TRect;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static info.qbnet.jtvision.core.views.TView.Options.*;
import static info.qbnet.jtvision.core.views.TView.State.*;

class TViewTest {

    static class TestableTView extends TView {
        private final TPalette palette;

        TestableTView(TRect bounds) {
            this(bounds, null);
        }

        TestableTView(TRect bounds, TPalette palette) {
            super(bounds);
            this.palette = palette;
        }

        @Override
        public void draw() {
            // no-op
        }

        @Override
        public TPalette getPalette() {
            return palette;
        }

        TPoint getOriginField() {
            return origin;
        }

        TPoint getSizeField() {
            return size;
        }
    }

    static class TestGroup extends TGroup {
        boolean resetCurrentCalled = false;
        private final TPalette palette;

        TestGroup(TRect bounds) {
            this(bounds, null);
        }

        TestGroup(TRect bounds, TPalette palette) {
            super(bounds);
            this.palette = palette;
        }

        @Override
        protected void resetCurrent() {
            resetCurrentCalled = true;
        }

        @Override
        public TPalette getPalette() {
            return palette;
        }
    }

    @Test
    void constructorInitializesGeometry() {
        TRect r = new TRect(new TPoint(1, 2), new TPoint(5, 6));
        TestableTView v = new TestableTView(r);
        assertSame(r.a, v.getOriginField());
        assertSame(r.b, v.getSizeField());
    }

    @Test
    void getExtentPopulatesProvidedRect() {
        TestableTView v = new TestableTView(new TRect(new TPoint(1, 2), new TPoint(5, 6)));
        TRect extent = new TRect();
        v.getExtent(extent);
        assertEquals(0, extent.a.x);
        assertEquals(0, extent.a.y);
        assertEquals(5, extent.b.x);
        assertEquals(6, extent.b.y);
    }

    @Test
    void hideClearsVisibleState() {
        TestableTView v = new TestableTView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        v.hide();
        assertEquals(0, v.getState() & SF_VISIBLE);
    }

    @Test
    void setStateWithMultipleBitsThrows() {
        TestableTView v = new TestableTView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        assertThrows(IllegalArgumentException.class, () ->
                v.setState(SF_VISIBLE | SF_ACTIVE, true));
    }

    @Test
    void setStateVisibleInvokesOwnerResetCurrent() throws Exception {
        TestGroup g = new TestGroup(new TRect(new TPoint(0,0), new TPoint(10,10)));
        TestableTView v = new TestableTView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        v.setOwner(g);

        Field optionsField = TView.class.getDeclaredField("options");
        optionsField.setAccessible(true);
        optionsField.setInt(v, OF_SELECTABLE);

        v.setState(SF_VISIBLE, false);
        assertEquals(0, v.getState() & SF_VISIBLE);
        assertTrue(g.resetCurrentCalled);

        g.resetCurrentCalled = false;

        v.setState(SF_VISIBLE, true);
        assertEquals(SF_VISIBLE, v.getState() & SF_VISIBLE);
        assertTrue(g.resetCurrentCalled);
    }

    @Test
    void mapColorMapsThroughOwnershipChain() {
        TRect r = new TRect(new TPoint(0,0), new TPoint(1,1));
        TestGroup root = new TestGroup(r, new TPalette(new byte[]{0x11, 0x22, 0x33}));
        TestGroup child = new TestGroup(r, new TPalette(new byte[]{2, 3, 1}));
        TestableTView leaf = new TestableTView(r, new TPalette(new byte[]{3, 1, 2}));
        child.setOwner(root);
        leaf.setOwner(child);

        short mapped = leaf.getColor((short)0x0201);
        assertEquals((short)0x2211, mapped);
    }

    @Test
    void mapColorReturnsErrorForInvalidOrZero() {
        TRect r = new TRect(new TPoint(0,0), new TPoint(1,1));
        TestableTView view = new TestableTView(r, new TPalette(new byte[]{0x11}));
        assertEquals((short)0xCFCF, view.getColor((short)0x0202));
        assertEquals((short)0xCFCF, view.getColor((short)0x0000));

        TestableTView zeroMap = new TestableTView(r, new TPalette(new byte[]{0x00}));
        assertEquals((short)0xCFCF, zeroMap.getColor((short)0x0101));
    }
}
