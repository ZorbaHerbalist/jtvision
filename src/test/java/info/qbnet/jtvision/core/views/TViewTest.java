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

    static class DrawCharView extends TView {
        DrawCharView(TRect bounds) { super(bounds); }

        @Override
        public void draw() {
            writeChar(0, 0, 'X', 0x07, 1);
        }
    }

    @Test
    void constructorInitializesGeometry() {
        TRect r = new TRect(new TPoint(1, 2), new TPoint(5, 6));
        TestableTView v = new TestableTView(r);
        assertEquals(r.a.x, v.getOriginField().x);
        assertEquals(r.a.y, v.getOriginField().y);
        assertEquals(r.b.x - r.a.x, v.getSizeField().x);
        assertEquals(r.b.y - r.a.y, v.getSizeField().y);
    }

    @Test
    void getExtentPopulatesProvidedRect() {
        TestableTView v = new TestableTView(new TRect(new TPoint(1, 2), new TPoint(5, 6)));
        TRect extent = new TRect();
        v.getExtent(extent);
        assertEquals(0, extent.a.x);
        assertEquals(0, extent.a.y);
        assertEquals(4, extent.b.x);
        assertEquals(4, extent.b.y);
    }

    @Test
    void hideClearsVisibleState() {
        TestableTView v = new TestableTView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        v.hide();
        assertEquals(0, v.state & SF_VISIBLE);
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
        assertEquals(0, v.state & SF_VISIBLE);
        assertTrue(g.resetCurrentCalled);

        g.resetCurrentCalled = false;

        v.setState(SF_VISIBLE, true);
        assertEquals(SF_VISIBLE, v.state & SF_VISIBLE);
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
        assertEquals((short)0x00CF, view.getColor((short)0x0000));

        TestableTView zeroMap = new TestableTView(r, new TPalette(new byte[]{0x00}));
        assertEquals((short)0xCFCF, zeroMap.getColor((short)0x0101));
    }

    @Test
    void writeViewTargetsNearestBufferedAncestor() {
        TGroup root = new TGroup(new TRect(new TPoint(0,0), new TPoint(3,3)));
        root.setState(SF_EXPOSED, true);
        TGroup mid = new TGroup(new TRect(new TPoint(0,0), new TPoint(3,3)));
        root.insert(mid);
        DrawCharView leaf = new DrawCharView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        mid.insert(leaf);

        root.draw();

        assertNotNull(mid.buffer);
        assertEquals('X', (char) (mid.buffer.getCell(0,0) & 0xFF));
    }

    @Test
    void writeViewSkipsDrawingWhenAncestorHidden() {
        TGroup root = new TGroup(new TRect(new TPoint(0,0), new TPoint(3,3)));
        root.setState(SF_EXPOSED, true);

        TGroup mid = new TGroup(new TRect(new TPoint(0,0), new TPoint(3,3)));
        root.insert(mid);

        DrawCharView leaf = new DrawCharView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        mid.insert(leaf);

        // Initial draw to allocate buffers and ensure mid has its own buffer.
        root.draw();
        assertNotNull(mid.buffer);

        // Hiding the mid group frees its buffer and clears its exposed state.
        mid.hide();
        assertNull(mid.buffer);

        // Capture the state of the root buffer before attempting to draw the leaf.
        int before = root.buffer.getCell(0, 0);

        // Drawing the leaf should not alter the root buffer because its ancestors
        // are no longer visible/exposed.
        leaf.draw();

        assertEquals(before, root.buffer.getCell(0, 0));
    }

    @Test
    void writeViewPropagatesThroughUnlockedAncestors() {
        TGroup root = new TGroup(new TRect(new TPoint(0,0), new TPoint(3,3)));
        root.setState(SF_EXPOSED, true);

        TGroup mid = new TGroup(new TRect(new TPoint(0,0), new TPoint(3,3)));
        root.insert(mid);

        DrawCharView leaf = new DrawCharView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        mid.insert(leaf);

        // Allocate buffers
        root.draw();

        // Clear any previous characters
        root.buffer.setChar(0, 0, ' ', 0);
        mid.buffer.setChar(0, 0, ' ', 0);

        leaf.draw();

        assertEquals('X', (char) (mid.buffer.getCell(0,0) & 0xFF));
        assertEquals('X', (char) (root.buffer.getCell(0,0) & 0xFF));
    }

    @Test
    void writeViewStopsAtLockedAncestor() {
        TGroup root = new TGroup(new TRect(new TPoint(0,0), new TPoint(3,3)));
        root.setState(SF_EXPOSED, true);

        TGroup mid = new TGroup(new TRect(new TPoint(0,0), new TPoint(3,3)));
        root.insert(mid);

        DrawCharView leaf = new DrawCharView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        mid.insert(leaf);

        root.draw();

        root.buffer.setChar(0, 0, ' ', 0);
        mid.buffer.setChar(0, 0, ' ', 0);

        mid.lock();
        leaf.draw();

        assertEquals('X', (char) (mid.buffer.getCell(0,0) & 0xFF));
        assertEquals(' ', (char) (root.buffer.getCell(0,0) & 0xFF));

        // Clean up lock to avoid side-effects
        mid.unlock();
    }

    @Test
    void forEachHandlesElementDeletingItself() {
        TGroup group = new TGroup(new TRect(new TPoint(0,0), new TPoint(1,1)));
        TestableTView view = new TestableTView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        group.insert(view);

        assertDoesNotThrow(() -> group.forEach(v -> group.delete(v)));
        assertNull(group.first());
    }

    @Test
    void calcBoundsRespectsGrowModeAndSizeLimits() {
        TGroup group = new TGroup(new TRect(new TPoint(0,0), new TPoint(10,10)));
        TestableTView view = new TestableTView(new TRect(new TPoint(0,0), new TPoint(5,6)));
        group.insert(view);
        view.growMode = TView.GrowMode.GF_GROW_HI_X | TView.GrowMode.GF_GROW_HI_Y;

        TRect original = new TRect();
        view.getBounds(original);

        TPoint delta = new TPoint(2,3);
        group.size.x += delta.x;
        group.size.y += delta.y;

        TRect bounds = new TRect();
        view.calcBounds(bounds, delta);

        TPoint min = new TPoint();
        TPoint max = new TPoint();
        view.sizeLimits(min, max);

        int newWidth = bounds.b.x - bounds.a.x;
        int newHeight = bounds.b.y - bounds.a.y;

        assertEquals(Math.min(original.b.x - original.a.x + delta.x, max.x), newWidth);
        assertEquals(Math.min(original.b.y - original.a.y + delta.y, max.y), newHeight);
        assertTrue(newWidth <= max.x);
        assertTrue(newHeight <= max.y);
    }
}
