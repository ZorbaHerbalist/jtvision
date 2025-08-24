package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.core.objects.TPoint;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.constants.KeyCode;
import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.views.support.CountingDrawView;
import info.qbnet.jtvision.core.views.support.DrawCharView;
import info.qbnet.jtvision.core.views.support.EventQueueView;
import info.qbnet.jtvision.core.views.support.FocusCountingGroup;
import info.qbnet.jtvision.core.views.support.MessageModifyingView;
import info.qbnet.jtvision.core.views.support.NonHandlingView;
import info.qbnet.jtvision.core.views.support.RefusingGroup;
import info.qbnet.jtvision.core.views.support.ShadowCountingView;
import info.qbnet.jtvision.core.views.support.TestGroup;
import info.qbnet.jtvision.core.views.support.TestableTView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static info.qbnet.jtvision.core.views.TView.Options.*;
import static info.qbnet.jtvision.core.views.TView.State.*;

class TViewTest {

    private Set<Integer> originalCommands;
    private boolean originalCommandSetChanged;
    private TView originalTopView;

    @BeforeEach
    void saveTViewState() {
        originalCommands = new java.util.HashSet<>(TView.getCommands());
        originalCommandSetChanged = TView.commandSetChanged;
        originalTopView = TView.theTopView;
    }

    @AfterEach
    void restoreTViewState() {
        TView.setCommands(originalCommands);
        TView.commandSetChanged = originalCommandSetChanged;
        TView.theTopView = originalTopView;
    }

    @Test
    void constructorInitializesGeometry() {
        TRect r = new TRect(1, 2, 5, 6);
        TestableTView v = new TestableTView(r);
        assertEquals(r.a.x, v.getOriginField().x);
        assertEquals(r.a.y, v.getOriginField().y);
        assertEquals(r.b.x - r.a.x, v.getSizeField().x);
        assertEquals(r.b.y - r.a.y, v.getSizeField().y);
    }

    @Test
    void getExtentPopulatesProvidedRect() {
        TestableTView v = new TestableTView(new TRect(1, 2, 5, 6));
        TRect extent = new TRect();
        v.getExtent(extent);
        assertEquals(0, extent.a.x);
        assertEquals(0, extent.a.y);
        assertEquals(4, extent.b.x);
        assertEquals(4, extent.b.y);
    }

    @Test
    void hideClearsVisibleState() {
        TestableTView v = new TestableTView(new TRect(0, 0, 1, 1));
        v.hide();
        assertEquals(0, v.state & SF_VISIBLE);
    }

    @Test
    void getHelpCtxReturnsDraggingContext() {
        TestableTView v = new TestableTView(new TRect(0, 0, 1, 1));
        v.setState(SF_DRAGGING, true);
        try {
            assertEquals(TView.HelpContext.HC_DRAGGING, v.getHelpCtx());
        } finally {
            // Reset state to avoid side effects
            v.setState(SF_DRAGGING, false);
        }
    }

    @Test
    void setStateWithMultipleBitsThrows() {
        TestableTView v = new TestableTView(new TRect(0, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () ->
                v.setState(SF_VISIBLE | SF_ACTIVE, true));
    }

    @Test
    void setStateVisibleInvokesOwnerResetCurrent() throws Exception {
        TestGroup g = new TestGroup(new TRect(0, 0, 10, 10));
        TestableTView v = new TestableTView(new TRect(0, 0, 1, 1));
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
        TRect r = new TRect(0, 0, 1, 1);
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
        TRect r = new TRect(0, 0, 1, 1);
        TestableTView view = new TestableTView(r, new TPalette(new byte[]{0x11}));
        assertEquals((short)0xCFCF, view.getColor((short)0x0202));
        assertEquals((short)0x00CF, view.getColor((short)0x0000));

        TestableTView zeroMap = new TestableTView(r, new TPalette(new byte[]{0x00}));
        assertEquals((short)0xCFCF, zeroMap.getColor((short)0x0101));
    }

    @Test
    void makeGlobalAndLocalRoundTripThroughOwnershipChain() {
        TGroup root = new TGroup(new TRect(10, 20, 30, 40));
        TGroup mid = new TGroup(new TRect(3, 4, 13, 14));
        root.insert(mid);
        TView leaf = new TView(new TRect(2, 1, 7, 6));
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
        TView view = new TView(new TRect(0, 0, 4, 4));
        view.moveTo(10, 20);

        TPoint inside = new TPoint(12, 22);
        assertTrue(view.mouseInView(inside));

        TPoint outside = new TPoint(14, 22);
        assertFalse(view.mouseInView(outside));
    }

    @Test
    void getClipRectUsesOwnerClipAndReturnsLocalCoordinates() {
        TGroup group = new TGroup(new TRect(0, 0, 10, 10));
        TView child = new TView(new TRect(1, 2, 10, 12));
        group.insert(child);
        group.clip.assign(2,3,7,8);

        TRect clip = new TRect();
        child.getClipRect(clip);

        assertEquals(1, clip.a.x);
        assertEquals(1, clip.a.y);
        assertEquals(6, clip.b.x);
        assertEquals(6, clip.b.y);
    }

    @Test
    void writeViewTargetsNearestBufferedAncestor() {
        TGroup root = new TGroup(new TRect(0, 0, 3, 3));
        root.setState(SF_EXPOSED, true);
        TGroup mid = new TGroup(new TRect(0, 0, 3, 3));
        root.insert(mid);
        DrawCharView leaf = new DrawCharView(new TRect(0, 0, 1, 1));
        mid.insert(leaf);

        root.draw();

        assertNotNull(mid.buffer);
        assertEquals('X', (char) (mid.buffer.getCell(0,0) & 0xFF));
    }

    @Test
    void writeViewSkipsDrawingWhenAncestorHidden() {
        TGroup root = new TGroup(new TRect(0, 0, 3, 3));
        root.setState(SF_EXPOSED, true);

        TGroup mid = new TGroup(new TRect(0, 0, 3, 3));
        root.insert(mid);

        DrawCharView leaf = new DrawCharView(new TRect(0, 0, 1, 1));
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
        TGroup root = new TGroup(new TRect(0, 0, 3, 3));
        root.setState(SF_EXPOSED, true);

        TGroup mid = new TGroup(new TRect(0, 0, 3, 3));
        root.insert(mid);

        DrawCharView leaf = new DrawCharView(new TRect(0, 0, 1, 1));
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
        TGroup root = new TGroup(new TRect(0, 0, 3, 3));
        root.setState(SF_EXPOSED, true);

        TGroup mid = new TGroup(new TRect(0, 0, 3, 3));
        root.insert(mid);

        DrawCharView leaf = new DrawCharView(new TRect(0, 0, 1, 1));
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
    void writeStrAndLinePropagate() {
        TGroup root = new TGroup(new TRect(0, 0, 5, 5));
        root.setState(SF_EXPOSED, true);

        TView child = new TView(new TRect(0, 0, 5, 5)) {
            @Override
            public void draw() {
                int color = 0x07;
                writeStr(0, 0, "ABC", color);
                TDrawBuffer buf = new TDrawBuffer();
                buf.moveStr(0, "DEF", color);
                writeLine(0, 1, 3, 1, buf.buffer);
            }
        };

        root.insert(child);
        root.draw();

        assertNotNull(root.buffer);
        assertEquals('A', (char) (root.buffer.getCell(0,0) & 0xFF));
        assertEquals('B', (char) (root.buffer.getCell(1,0) & 0xFF));
        assertEquals('C', (char) (root.buffer.getCell(2,0) & 0xFF));
        assertEquals('D', (char) (root.buffer.getCell(0,1) & 0xFF));
        assertEquals('E', (char) (root.buffer.getCell(1,1) & 0xFF));
        assertEquals('F', (char) (root.buffer.getCell(2,1) & 0xFF));
    }

    @Test
    void forEachHandlesElementDeletingItself() {
        TGroup group = new TGroup(new TRect(0, 0, 1, 1));
        TestableTView view = new TestableTView(new TRect(0, 0, 1, 1));
        group.insert(view);

        assertDoesNotThrow(() -> group.forEach(v -> group.delete(v)));
        assertNull(group.first());
    }

    @Test
    void makeFirstMovesLastViewToFront() {
        TGroup group = new TGroup(new TRect(0, 0, 1, 1));
        TestableTView v1 = new TestableTView(new TRect(0, 0, 1, 1));
        TestableTView v2 = new TestableTView(new TRect(0, 0, 1, 1));
        TestableTView v3 = new TestableTView(new TRect(0, 0, 1, 1));
        group.insert(v1);
        group.insert(v2);
        group.insert(v3);

        TView last = group.first().prev();
        last.makeFirst();

        assertSame(last, group.first());
    }

    @Test
    void selectWithTopSelectMovesAndFocuses() {
        TGroup group = new TGroup(new TRect(0, 0, 1, 1));
        group.setState(SF_ACTIVE, true);
        group.setState(SF_FOCUSED, true);

        TView top = new TView(new TRect(0, 0, 1, 1));
        TView other = new TView(new TRect(0, 0, 1, 1));
        group.insert(top);
        group.insert(other);

        top.options = OF_SELECTABLE | OF_TOP_SELECT;
        top.select();

        assertSame(top, group.first());
        assertEquals(SF_SELECTED | SF_FOCUSED, top.state & (SF_SELECTED | SF_FOCUSED));
    }

    @Test
    void calcBoundsRespectsGrowModeAndSizeLimits() {
        TGroup group = new TGroup(new TRect(0, 0, 10, 10));
        TestableTView view = new TestableTView(new TRect(0, 0, 5, 6));
        group.insert(view);
        view.growMode = TView.GrowMode.GF_GROW_HI_X | TView.GrowMode.GF_GROW_HI_Y;

        int originalWidth  = view.getSizeField().x;
        int originalHeight = view.getSizeField().y;

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

        assertEquals(Math.min(originalWidth  + delta.x, max.x), newWidth);
        assertEquals(Math.min(originalHeight + delta.y, max.y), newHeight);
        assertTrue(newWidth <= max.x);
        assertTrue(newHeight <= max.y);
    }

    @Test
    void calcBoundsScalesProportionallyWithGrowRel() {
        TestGroup group = new TestGroup(new TRect(0, 0, 100, 100));
        TestableTView view = new TestableTView(new TRect(10, 10, 30, 30));
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
        EventQueueView view = new EventQueueView(new TRect(0, 0, 1, 1));
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
        EventQueueView view = new EventQueueView(new TRect(0, 0, 1, 1));
        TEvent ev = new TEvent();
        ev.what = TEvent.EV_COMMAND;
        view.putEvent(ev);

        assertTrue(view.eventAvail());

        TEvent fetched = new TEvent();
        view.getEvent(fetched);
        assertEquals(ev.what, fetched.what);
    }

    @Test
    void clearEventResetsEvent() {
        TestableTView view = new TestableTView(new TRect(0, 0, 1, 1));
        TEvent event = new TEvent();
        event.what = TEvent.EV_KEYDOWN;
        event.msg.infoPtr = new Object();

        view.clearEvent(event);

        assertEquals(TEvent.EV_NOTHING, event.what);
        assertSame(view, event.msg.infoPtr);
    }

    @Test
    void mouseEventStopsAtUpAndMatchesDown() {
        EventQueueView view = new EventQueueView(new TRect(0, 0, 1, 1));

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
    void dragViewMovesWithMouse() {
        TGroup parent = new TGroup(new TRect(0, 0, 10, 10));
        EventQueueView view = new EventQueueView(new TRect(0, 0, 1, 1));
        parent.insert(view);

        TEvent move1 = new TEvent();
        move1.what = TEvent.EV_MOUSE_MOVE;
        move1.mouse.where.x = 3;
        move1.mouse.where.y = 3;

        TEvent move2 = new TEvent();
        move2.what = TEvent.EV_MOUSE_MOVE;
        move2.mouse.where.x = 7;
        move2.mouse.where.y = 7;

        TEvent up = new TEvent();
        up.what = TEvent.EV_MOUSE_UP;
        up.mouse.where.x = 7;
        up.mouse.where.y = 7;

        view.events.add(move1);
        view.events.add(move2);
        view.events.add(up);

        TEvent down = new TEvent();
        down.what = TEvent.EV_MOUSE_DOWN;
        down.mouse.where.x = 0;
        down.mouse.where.y = 0;

        TRect limits = new TRect(0, 0, 5, 5);
        view.dragView(down, TView.DragMode.DM_DRAG_MOVE | TView.DragMode.DM_LIMIT_ALL,
                limits, new TPoint(1,1), new TPoint(1,1));

        assertEquals(4, view.getOriginField().x);
        assertEquals(4, view.getOriginField().y);
    }

    @Test
    void dragViewMovesOriginRightWithKeyboard() {
        TGroup parent = new TGroup(new TRect(0, 0, 10, 10));
        EventQueueView view = new EventQueueView(new TRect(0, 0, 1, 1));
        parent.insert(view);

        TEvent right = new TEvent();
        right.what = TEvent.EV_KEYDOWN;
        right.key.keyCode = KeyCode.KB_RIGHT;
        TEvent enter = new TEvent();
        enter.what = TEvent.EV_KEYDOWN;
        enter.key.keyCode = KeyCode.KB_ENTER;
        view.events.add(right);
        view.events.add(enter);

        TRect limits = new TRect(0, 0, 10, 10);
        view.dragView(new TEvent(), TView.DragMode.DM_DRAG_MOVE, limits, new TPoint(1,1), new TPoint(1,1));

        assertEquals(1, view.getOriginField().x);
        assertEquals(0, view.getOriginField().y);
    }

    @Test
    void dragViewGrowClipsSizeWithinBounds() {
        EventQueueView view = new EventQueueView(new TRect(0, 0, 4, 4));
        TRect limits = new TRect(0, 0, 100, 100);
        TPoint minSize = new TPoint(2,2);
        TPoint maxSize = new TPoint(6,6);

        TEvent down = new TEvent();
        down.what = TEvent.EV_MOUSE_DOWN;
        down.mouse.where.x = view.getSizeField().x;
        down.mouse.where.y = view.getSizeField().y;

        TEvent move = new TEvent();
        move.what = TEvent.EV_MOUSE_MOVE;
        move.mouse.where.x = 100;
        move.mouse.where.y = 100;

        TEvent up = new TEvent();
        up.what = TEvent.EV_MOUSE_UP;

        view.events.add(move);
        view.events.add(up);

        view.dragView(down, TView.DragMode.DM_DRAG_GROW, limits, minSize, maxSize);

        assertEquals(maxSize.x, view.getSizeField().x);
        assertEquals(maxSize.y, view.getSizeField().y);
    }

    @Test
    void dragViewKeepsOriginWithinLimitsWhenMovingRightRepeatedly() {
        TGroup parent = new TGroup(new TRect(0, 0, 10, 10));
        EventQueueView view = new EventQueueView(new TRect(0, 0, 1, 1));
        parent.insert(view);

        TRect limits = new TRect(0, 0, 3, 3);
        int maxX = limits.b.x - view.getSizeField().x;
        int maxY = limits.b.y - view.getSizeField().y;

        for (int i = 0; i < 5; i++) {
            TEvent right = new TEvent();
            right.what = TEvent.EV_KEYDOWN;
            right.key.keyCode = KeyCode.KB_RIGHT;
            TEvent enter = new TEvent();
            enter.what = TEvent.EV_KEYDOWN;
            enter.key.keyCode = KeyCode.KB_ENTER;
            view.events.add(right);
            view.events.add(enter);

            view.dragView(new TEvent(),
                    TView.DragMode.DM_DRAG_MOVE | TView.DragMode.DM_LIMIT_ALL,
                    limits,
                    new TPoint(1, 1),
                    new TPoint(1, 1));

            assertTrue(view.getOriginField().x >= limits.a.x);
            assertTrue(view.getOriginField().x <= maxX);
            assertTrue(view.getOriginField().y >= limits.a.y);
            assertTrue(view.getOriginField().y <= maxY);
        }

        assertEquals(maxX, view.getOriginField().x);
    }



    @Test
    void drawViewHonorsExposedState() {
        TGroup group = new TGroup(new TRect(0, 0, 1, 1));
        CountingDrawView view = new CountingDrawView(new TRect(0, 0, 1, 1));
        group.insert(view);

        view.setState(SF_VISIBLE, true);
        view.setState(SF_EXPOSED, true);
        view.drawView();
        assertEquals(1, view.drawCount);

        view.setState(SF_EXPOSED, false);
        view.drawView();
        assertEquals(1, view.drawCount);
    }

    @Test
    void showSetsVisibleAndDraws() {
        TGroup root = new TGroup(new TRect(0, 0, 1, 1));
        root.setState(SF_EXPOSED, true);
        CountingDrawView view = new CountingDrawView(new TRect(0, 0, 1, 1));
        view.setState(SF_VISIBLE, false);
        root.insert(view);

        assertEquals(0, view.drawCount);
        assertEquals(0, view.state & SF_VISIBLE);

        view.show();

        assertEquals(1, view.drawCount);
        assertEquals(SF_VISIBLE, view.state & SF_VISIBLE);
    }


    @Test
    void exposedReturnsFalseWhenFullyCoveredAndTrueWhenPartiallyVisible() {
        TGroup root = new TGroup(new TRect(0, 0, 10, 10));
        root.setState(SF_VISIBLE, true);
        root.setState(SF_EXPOSED, true);

        TestableTView back = new TestableTView(new TRect(0, 0, 3, 3));
        root.insert(back);
        back.setState(SF_VISIBLE, true);
        back.setState(SF_EXPOSED, true);

        TestableTView front = new TestableTView(new TRect(1, 1, 4, 4));
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
        RefusingGroup owner = new RefusingGroup(new TRect(0, 0, 1, 1));
        TestableTView view = new TestableTView(new TRect(0, 0, 1, 1));
        view.options = OF_SELECTABLE;
        owner.insert(view);
        view.setState(SF_SELECTED, false);
        owner.clearSelection();
        assertFalse(view.focus());
        assertEquals(0, view.state & SF_SELECTED);
    }

    @Test
    void shadowFlagInvokesDrawUnderViewAndFocusedBroadcasts() {
        FocusCountingGroup owner = new FocusCountingGroup(new TRect(0, 0, 1, 1));
        ShadowCountingView view = new ShadowCountingView(new TRect(0, 0, 1, 1));
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
        Set<Integer> cmds = Set.of(Command.CM_HELP);
        TView.commandSetChanged = false;
        TView.disableCommands(cmds);
        assertFalse(TView.commandEnabled(Command.CM_HELP));
        assertTrue(TView.commandSetChanged);

        TView.commandSetChanged = false;
        TView.enableCommands(cmds);
        assertTrue(TView.commandEnabled(Command.CM_HELP));
        assertTrue(TView.commandSetChanged);
    }

    @Test
    void commandEnabledIgnoresHighCodes() {
        int highCommand = 0x1000;
        Set<Integer> original = TView.getCommands();
        boolean changed = TView.commandSetChanged;

        assertFalse(original.contains(highCommand));
        assertTrue(TView.commandEnabled(highCommand));
        assertEquals(original, TView.getCommands());
        assertEquals(changed, TView.commandSetChanged);
    }

    @Test
    void commandEnabledReturnsTrueForUserCommand() {
        Set<Integer> original = TView.getCommands();
        Set<Integer> cmds = new java.util.HashSet<>(original);
        cmds.remove(Command.CM_HELP); // remove an entry from curCommandSet
        TView.setCommands(cmds);
        assertFalse(TView.commandEnabled(Command.CM_HELP));

        int userCommand = 300; // Command.CM_USER + 1
        assertTrue(TView.commandEnabled(userCommand));
    }

    @ParameterizedTest
    @MethodSource("messageParams")
    void messageReturnsModifiedObject(Object param) {
        MessageModifyingView receiver = new MessageModifyingView();
        Object result = TView.message(receiver, TEvent.EV_COMMAND, Command.CM_OK, param);
        assertEquals("modified", result);
    }

    private static Stream<Object> messageParams() {
        return Stream.of("data", 42);
    }

    @Test
    void messageReturnsNullWhenNotHandled() {
        NonHandlingView receiver = new NonHandlingView();
        Object result = TView.message(receiver, TEvent.EV_COMMAND, Command.CM_OK, "data");
        assertNull(result);
    }

    @Test
    void topViewReturnsModalAncestor() {
        TGroup root = new TGroup(new TRect(0, 0, 10, 10));
        TGroup modal = new TGroup(new TRect(0, 0, 10, 10));
        TView leaf = new TView(new TRect(0, 0, 1, 1));
        root.insert(modal);
        modal.insert(leaf);
        modal.setState(SF_MODAL, true);

        TView.theTopView = null;
        assertSame(modal, leaf.topView());
    }

    @Test
    void endModalPropagatesToTop() {
        TGroup root = new TGroup(new TRect(0, 0, 1, 1));
        TGroup modal = new TGroup(new TRect(0, 0, 1, 1));
        TView child = new TView(new TRect(0, 0, 1, 1));

        root.insert(modal);
        modal.insert(child);
        modal.setState(SF_MODAL, true);

        child.endModal(Command.CM_OK);

        assertEquals(Command.CM_OK, modal.endState);
    }
}
