package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.core.objects.TPoint;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.constants.KeyCode;
import info.qbnet.jtvision.core.constants.Command;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Set;

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

    static class CountingDrawView extends TView {
        int drawCount = 0;

        CountingDrawView(TRect bounds) { super(bounds); }

        @Override
        public void draw() {
            drawCount++;
        }
    }

    static class EventQueueView extends TestableTView {
        java.util.ArrayDeque<TEvent> events = new java.util.ArrayDeque<>();

        EventQueueView(TRect bounds) { super(bounds); }

        @Override
        public void getEvent(TEvent event) {
            if (!events.isEmpty()) {
                event.copyFrom(events.removeFirst());
            } else {
                event.what = TEvent.EV_NOTHING;
            }
        }

        @Override
        public void putEvent(TEvent event) {
            events.addFirst(event);
        }
    }

    static class ShadowCountingView extends TestableTView {
        int drawUnderViewCalls = 0;

        ShadowCountingView(TRect bounds) { super(bounds); }

        @Override
        protected void drawUnderView(boolean doShadow, TView lastView) {
            drawUnderViewCalls++;
        }
    }

    static class FocusCountingGroup extends TestGroup {
        int receivedFocus = 0;

        FocusCountingGroup(TRect bounds) { super(bounds); }

        @Override
        public void handleEvent(TEvent event) {
            if (event.what == TEvent.EV_BROADCAST && event.msg.command == Command.CM_RECEIVED_FOCUS) {
                receivedFocus++;
                event.what = TEvent.EV_NOTHING;
            }
            super.handleEvent(event);
        }
    }

    static class RefusingGroup extends TGroup {
        RefusingGroup(TRect bounds) { super(bounds); }

        @Override
        public boolean focus() {
            return false;
        }

        void clearSelection() {
            current = null;
        }
    }

    static class MessageModifyingView extends TView {
        MessageModifyingView() {
            super(new TRect(new TPoint(0,0), new TPoint(1,1)));
        }

        @Override
        public void draw() {
            // no-op
        }

        @Override
        public void handleEvent(TEvent event) {
            if (event.what == TEvent.EV_COMMAND && event.msg.command == Command.CM_OK) {
                event.msg.infoPtr = "modified";
                event.what = TEvent.EV_NOTHING;
            }
            super.handleEvent(event);
        }
    }

    static class NonHandlingView extends TView {
        NonHandlingView() {
            super(new TRect(new TPoint(0,0), new TPoint(1,1)));
        }

        @Override
        public void draw() {
            // no-op
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
    void makeGlobalAndLocalRoundTripThroughOwnershipChain() {
        TGroup root = new TGroup(new TRect(new TPoint(10,20), new TPoint(30,40)));
        TGroup mid = new TGroup(new TRect(new TPoint(3,4), new TPoint(13,14)));
        root.insert(mid);
        TView leaf = new TView(new TRect(new TPoint(2,1), new TPoint(7,6)));
        mid.insert(leaf);

        TPoint local = new TPoint(1,2);
        TPoint global = new TPoint();
        leaf.makeGlobal(local, global);

        assertEquals(16, global.x);
        assertEquals(27, global.y);

        TPoint roundTrip = new TPoint();
        leaf.makeLocal(global, roundTrip);
        assertEquals(local.x, roundTrip.x);
        assertEquals(local.y, roundTrip.y);
    }

    @Test
    void mouseInViewChecksGlobalCoordinates() {
        TView view = new TView(new TRect(new TPoint(0,0), new TPoint(4,4)));
        view.moveTo(10, 20);

        TPoint inside = new TPoint(12, 22);
        assertTrue(view.mouseInView(inside));

        TPoint outside = new TPoint(14, 22);
        assertFalse(view.mouseInView(outside));
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
    void makeFirstMovesLastViewToFront() {
        TGroup group = new TGroup(new TRect(new TPoint(0,0), new TPoint(1,1)));
        TestableTView v1 = new TestableTView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        TestableTView v2 = new TestableTView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        TestableTView v3 = new TestableTView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        group.insert(v1);
        group.insert(v2);
        group.insert(v3);

        TView last = group.first().prev();
        last.makeFirst();

        assertSame(last, group.first());
    }

    @Test
    void selectWithTopSelectMovesAndFocuses() {
        TGroup group = new TGroup(new TRect(new TPoint(0,0), new TPoint(1,1)));
        group.setState(SF_ACTIVE, true);
        group.setState(SF_FOCUSED, true);

        TView top = new TView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        TView other = new TView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        group.insert(top);
        group.insert(other);

        top.options = OF_SELECTABLE | OF_TOP_SELECT;
        top.select();

        assertSame(top, group.first());
        assertEquals(SF_SELECTED | SF_FOCUSED, top.state & (SF_SELECTED | SF_FOCUSED));
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

    @Test
    void calcBoundsScalesProportionallyWithGrowRel() {
        TestGroup group = new TestGroup(new TRect(new TPoint(0,0), new TPoint(100,100)));
        TestableTView view = new TestableTView(new TRect(new TPoint(10,10), new TPoint(30,30)));
        group.insert(view);
        view.growMode = TView.GrowMode.GF_GROW_ALL | TView.GrowMode.GF_GROW_REL;

        TPoint delta = new TPoint(10,20);
        group.size.x += delta.x;
        group.size.y += delta.y;

        TRect bounds = new TRect();
        view.calcBounds(bounds, delta);

        assertEquals(11, bounds.a.x);
        assertEquals(12, bounds.a.y);
        assertEquals(33, bounds.b.x);
        assertEquals(36, bounds.b.y);
    }

    @Test
    void eventAvailDetectsAndConsumesEvent() {
        EventQueueView view = new EventQueueView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        TEvent ev = new TEvent();
        ev.what = TEvent.EV_KEYDOWN;
        view.events.add(ev);

        assertTrue(view.eventAvail());

        TEvent fetched = new TEvent();
        view.getEvent(fetched);
        assertEquals(TEvent.EV_KEYDOWN, fetched.what);
        view.clearEvent(fetched);

        assertFalse(view.eventAvail());
    }

    @Test
    void eventAvailReturnsTrueAndGetEventReturnsSameEvent() {
        EventQueueView view = new EventQueueView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        TEvent ev = new TEvent();
        ev.what = TEvent.EV_COMMAND;
        view.putEvent(ev);

        assertTrue(view.eventAvail());

        TEvent fetched = new TEvent();
        view.getEvent(fetched);
        assertEquals(ev.what, fetched.what);
    }

    @Test
    void mouseEventStopsAtUpAndMatchesDown() {
        EventQueueView view = new EventQueueView(new TRect(new TPoint(0,0), new TPoint(1,1)));

        TEvent down = new TEvent();
        down.what = TEvent.EV_MOUSE_DOWN;
        TEvent move = new TEvent();
        move.what = TEvent.EV_MOUSE_MOVE;
        TEvent up = new TEvent();
        up.what = TEvent.EV_MOUSE_UP;

        view.putEvent(down);
        view.putEvent(move);
        view.putEvent(up);

        TEvent event = new TEvent();
        assertFalse(view.mouseEvent(event, TEvent.EV_MOUSE_MOVE));
        assertEquals(TEvent.EV_MOUSE_UP, event.what);

        assertTrue(view.mouseEvent(event, TEvent.EV_MOUSE_DOWN));
        assertEquals(TEvent.EV_MOUSE_DOWN, event.what);
    }

    @Test
    void dragViewMovesOriginRightWithKeyboard() {
        TGroup parent = new TGroup(new TRect(new TPoint(0,0), new TPoint(10,10)));
        EventQueueView view = new EventQueueView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        parent.insert(view);

        TEvent right = new TEvent();
        right.what = TEvent.EV_KEYDOWN;
        right.key.keyCode = KeyCode.KB_RIGHT;
        TEvent enter = new TEvent();
        enter.what = TEvent.EV_KEYDOWN;
        enter.key.keyCode = KeyCode.KB_ENTER;
        view.events.add(right);
        view.events.add(enter);

        TRect limits = new TRect(new TPoint(0,0), new TPoint(10,10));
        view.dragView(new TEvent(), TView.DragMode.DM_DRAG_MOVE, limits, new TPoint(1,1), new TPoint(1,1));

        assertEquals(1, view.getOriginField().x);
        assertEquals(0, view.getOriginField().y);
    }

    @Test
    void drawViewHonorsExposedState() {
        TGroup group = new TGroup(new TRect(new TPoint(0,0), new TPoint(1,1)));
        CountingDrawView view = new CountingDrawView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        group.insert(view);

        view.drawView();
        assertEquals(0, view.drawCount);

        view.setState(SF_EXPOSED, true);
        view.drawView();
        assertEquals(1, view.drawCount);
    }

    @Test
    void exposedReturnsFalseWhenFullyCoveredAndTrueWhenPartiallyVisible() {
        TGroup root = new TGroup(new TRect(new TPoint(0,0), new TPoint(10,10)));
        root.setState(SF_VISIBLE, true);
        root.setState(SF_EXPOSED, true);

        TestableTView back = new TestableTView(new TRect(new TPoint(0,0), new TPoint(3,3)));
        root.insert(back);
        back.setState(SF_VISIBLE, true);
        back.setState(SF_EXPOSED, true);

        TestableTView front = new TestableTView(new TRect(new TPoint(1,1), new TPoint(4,4)));
        root.insert(front);
        front.setState(SF_VISIBLE, true);
        front.setState(SF_EXPOSED, true);

        front.moveTo(0, 0);
        assertFalse(back.exposed());

        front.moveTo(1, 1);
        assertTrue(back.exposed());
    }

    @Test
    void focusReturnsFalseAndDoesNotSelectWhenOwnerRefuses() {
        RefusingGroup owner = new RefusingGroup(new TRect(new TPoint(0,0), new TPoint(1,1)));
        TestableTView view = new TestableTView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        view.options = OF_SELECTABLE;
        owner.insert(view);
        view.setState(SF_SELECTED, false);
        owner.clearSelection();
        assertFalse(view.focus());
        assertEquals(0, view.state & SF_SELECTED);
    }

    @Test
    void shadowFlagInvokesDrawUnderViewAndFocusedBroadcasts() {
        FocusCountingGroup owner = new FocusCountingGroup(new TRect(new TPoint(0,0), new TPoint(1,1)));
        ShadowCountingView view = new ShadowCountingView(new TRect(new TPoint(0,0), new TPoint(1,1)));
        view.setOwner(owner);

        view.setState(SF_VISIBLE, true);
        assertEquals(0, view.drawUnderViewCalls);

        view.setState(SF_SHADOW, true);
        assertEquals(1, view.drawUnderViewCalls);

        view.setState(SF_FOCUSED, true);
        assertEquals(1, owner.receivedFocus);
    }

    @Test
    void disableCommandsDisablesAndEnableRestores() {
        Set<Integer> original = TView.getCommands();
        boolean changed = TView.commandSetChanged;
        try {
            Set<Integer> cmds = Set.of(Command.CM_HELP);
            TView.commandSetChanged = false;
            TView.disableCommands(cmds);
            assertFalse(TView.commandEnabled(Command.CM_HELP));
            assertTrue(TView.commandSetChanged);

            TView.commandSetChanged = false;
            TView.enableCommands(cmds);
            assertTrue(TView.commandEnabled(Command.CM_HELP));
            assertTrue(TView.commandSetChanged);
        } finally {
            TView.setCommands(original);
            TView.commandSetChanged = changed;
        }
    }

    @Test
    void messageReturnsModifiedObject() {
        MessageModifyingView receiver = new MessageModifyingView();
        Object result = TView.message(receiver, TEvent.EV_COMMAND, Command.CM_OK, "data");
        assertEquals("modified", result);
    }

    @Test
    void messageReturnsModifiedObjectFromInt() {
        MessageModifyingView receiver = new MessageModifyingView();
        Object result = TView.message(receiver, TEvent.EV_COMMAND, Command.CM_OK, 42);
        assertEquals("modified", result);
    }

    @Test
    void messageReturnsNullWhenNotHandled() {
        NonHandlingView receiver = new NonHandlingView();
        Object result = TView.message(receiver, TEvent.EV_COMMAND, Command.CM_OK, "data");
        assertNull(result);
    }

    @Test
    void topViewReturnsModalAncestor() {
        TView originalTop = TView.theTopView;
        try {
            TGroup root = new TGroup(new TRect(new TPoint(0, 0), new TPoint(10, 10)));
            TGroup modal = new TGroup(new TRect(new TPoint(0, 0), new TPoint(10, 10)));
            TView leaf = new TView(new TRect(new TPoint(0, 0), new TPoint(1, 1)));
            root.insert(modal);
            modal.insert(leaf);
            modal.setState(SF_MODAL, true);

            TView.theTopView = null;
            assertSame(modal, leaf.topView());
        } finally {
            TView.theTopView = originalTop;
        }
    }

    @Test
    void endModalPropagatesToTop() {
        TGroup root = new TGroup(new TRect(new TPoint(0, 0), new TPoint(1, 1)));
        TGroup modal = new TGroup(new TRect(new TPoint(0, 0), new TPoint(1, 1)));
        TView child = new TView(new TRect(new TPoint(0, 0), new TPoint(1, 1)));

        root.insert(modal);
        modal.insert(child);
        modal.setState(SF_MODAL, true);

        child.endModal(Command.CM_OK);

        assertEquals(Command.CM_OK, modal.endState);
    }
}
