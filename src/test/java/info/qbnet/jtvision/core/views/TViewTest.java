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
        TestableTView(TRect bounds) {
            super(bounds);
        }

        @Override
        public void draw() {
            // no-op
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

        TestGroup(TRect bounds) {
            super(bounds);
        }

        @Override
        protected void resetCurrent() {
            resetCurrentCalled = true;
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
}
