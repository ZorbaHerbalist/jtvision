package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.views.support.CountingDrawView;
import info.qbnet.jtvision.views.support.DrawCharView;
import info.qbnet.jtvision.views.support.EventQueueView;
import info.qbnet.jtvision.views.support.FocusCountingGroup;
import info.qbnet.jtvision.views.support.MessageModifyingView;
import info.qbnet.jtvision.views.support.NonHandlingView;
import info.qbnet.jtvision.views.support.RefusingGroup;
import info.qbnet.jtvision.views.support.ShadowCountingView;
import info.qbnet.jtvision.views.support.TestGroup;
import info.qbnet.jtvision.views.support.TestableTView;
import info.qbnet.jtvision.util.PaletteRole;
import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.PaletteFactory;
import info.qbnet.jtvision.util.TPalette;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static info.qbnet.jtvision.event.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static info.qbnet.jtvision.views.TView.Options.*;
import static info.qbnet.jtvision.views.TView.State.*;

class TViewTest {

    private enum TestPaletteRole implements PaletteRole {
        INDEX1(0x01),
        INDEX2(0x02),
        INDEX3(0x03);

        private final byte defaultValue;

        TestPaletteRole(int defaultValue) {
            this.defaultValue = PaletteRole.toByte(defaultValue);
        }

        @Override
        public byte defaultValue() {
            return defaultValue;
        }
    }

    private static TPalette palette(int... values) {
        EnumMap<TestPaletteRole, Byte> map = new EnumMap<>(TestPaletteRole.class);
        if (values.length > TestPaletteRole.values().length) {
            throw new IllegalArgumentException("Test palette supports up to "
                    + TestPaletteRole.values().length + " entries");
        }
        for (int i = 0; i < values.length; i++) {
            map.put(TestPaletteRole.values()[i], (byte) values[i]);
        }
        return new TPalette(map);
    }

    private Set<Integer> originalCommands;
    private boolean originalCommandSetChanged;
    private TView originalTopView;
    private EventQueueView queueView;
    private PaletteFactory.MissingEntryPolicy originalMissingEntryPolicy;

    @BeforeEach
    void saveTViewState() {
        originalCommands = new java.util.HashSet<>(TView.getCommands());
        originalCommandSetChanged = TView.commandSetChanged;
        originalTopView = TView.theTopView;
        originalMissingEntryPolicy = PaletteFactory.getMissingEntryPolicy();
        PaletteFactory.setMissingEntryPolicy(PaletteFactory.MissingEntryPolicy.LOG);
    }

    @BeforeEach
    void initEventQueueView() {
        queueView = new EventQueueView(new TRect(0, 0, 1, 1));
    }

    @AfterEach
    void restoreTViewState() {
        TView.setCommands(originalCommands);
        TView.commandSetChanged = originalCommandSetChanged;
        TView.theTopView = originalTopView;
        PaletteFactory.setMissingEntryPolicy(originalMissingEntryPolicy);
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
    void setStateVisibleInvokesOwnerResetCurrent() {
        TestGroup g = new TestGroup(new TRect(0, 0, 10, 10));
        TestableTView v = new TestableTView(new TRect(0, 0, 1, 1));
        v.setOwner(g);

        TViewTestAccess.setOptions(v, OF_SELECTABLE);

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
        TestGroup root = new TestGroup(r, palette(0x11, 0x22, 0x33));
        TestGroup child = new TestGroup(r, palette(2, 3, 1));
        TestableTView leaf = new TestableTView(r, palette(3, 1, 2));
        child.setOwner(root);
        leaf.setOwner(child);

        short mapped = leaf.getColor(TestPaletteRole.INDEX1, TestPaletteRole.INDEX2);
        assertEquals((short)0x2211, mapped);
    }

    @Test
    void mapColorReturnsErrorForInvalidOrZero() {
        TRect r = new TRect(0, 0, 1, 1);
        TestableTView view = new TestableTView(r, palette(0x11));
        assertEquals((short)0xCFCF, view.getColor(TestPaletteRole.INDEX2, TestPaletteRole.INDEX2));
        assertEquals((short)0x00CF, view.getColor((short)0x0000));

        TestableTView zeroMap = new TestableTView(r, palette(0x00));
        assertEquals((short)0xCFCF, zeroMap.getColor(TestPaletteRole.INDEX1, TestPaletteRole.INDEX1));
    }

    @Test
    void mapColorReturnsErrorWhenPaletteEntryMissing() {
        TRect r = new TRect(0, 0, 1, 1);
        TestGroup root = new TestGroup(r, palette(0x11));
        TestableTView child = new TestableTView(r);
        child.setOwner(root);

        assertEquals((short)0xCFCF, child.getColor(TestPaletteRole.INDEX2, TestPaletteRole.INDEX2));
    }

    @Test
    void mapColorLogsWarningForMissingEntriesInCompatibilityMode() {
        TRect r = new TRect(0, 0, 1, 1);
        TestGroup root = new TestGroup(r, palette(0x11));
        TestableTView child = new TestableTView(r);
        child.setOwner(root);

        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(TestableTView.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertEquals((short) 0xCFCF, child.getColor(TestPaletteRole.INDEX2, TestPaletteRole.INDEX2));
        } finally {
            logger.detachAppender(appender);
        }

        assertFalse(appender.list.isEmpty(), "Expected warning log for missing palette entry");
        ILoggingEvent event = appender.list.get(0);
        assertEquals(ch.qos.logback.classic.Level.WARN, event.getLevel());
        String message = event.getFormattedMessage();
        assertTrue(message.contains("palette index 2"));
        assertTrue(message.contains(child.getLogName()));
        assertTrue(message.contains(root.getLogName()));
    }

    @Test
    void mapColorThrowsWhenStrictMissingEntryPolicyEnabled() {
        PaletteFactory.setMissingEntryPolicy(PaletteFactory.MissingEntryPolicy.THROW);

        TRect r = new TRect(0, 0, 1, 1);
        TestGroup root = new TestGroup(r, palette(0x11));
        TestableTView child = new TestableTView(r);
        child.setOwner(root);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> child.getColor(TestPaletteRole.INDEX2, TestPaletteRole.INDEX2));
        String message = exception.getMessage();
        assertTrue(message.contains("palette index"));
        assertTrue(message.contains(child.getLogName()));
        assertTrue(message.contains(root.getLogName()));
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

        TViewTestAccess.setOptions(top, OF_SELECTABLE | OF_TOP_SELECT);
        top.select();

        assertSame(top, group.first());
        assertEquals(SF_SELECTED | SF_FOCUSED, top.state & (SF_SELECTED | SF_FOCUSED));
    }

    @Test
    void calcBoundsRespectsGrowModeAndSizeLimits() {
        TGroup group = new TGroup(new TRect(0, 0, 10, 10));
        TestableTView view = new TestableTView(new TRect(0, 0, 5, 6));
        group.insert(view);
        view.setGrowModes(EnumSet.of(TView.GrowMode.GF_GROW_HI_X, TView.GrowMode.GF_GROW_HI_Y));

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
        view.clearGrowModes();
        view.setGrowModes(TView.GrowMode.growAll());
        view.addGrowMode(TView.GrowMode.GF_GROW_REL);

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
    void growAllReturnsIndependentSets() {
        EnumSet<TView.GrowMode> first = TView.GrowMode.growAll();
        first.remove(TView.GrowMode.GF_GROW_LO_X);
        EnumSet<TView.GrowMode> second = TView.GrowMode.growAll();
        assertEquals(EnumSet.of(TView.GrowMode.GF_GROW_LO_X,
                TView.GrowMode.GF_GROW_LO_Y,
                TView.GrowMode.GF_GROW_HI_X,
                TView.GrowMode.GF_GROW_HI_Y), second);
    }

    @Test
    void limitAllReturnsIndependentSets() {
        EnumSet<TView.DragMode> first = TView.DragMode.limitAll();
        first.remove(TView.DragMode.DM_LIMIT_LO_X);
        EnumSet<TView.DragMode> second = TView.DragMode.limitAll();
        assertEquals(EnumSet.of(TView.DragMode.DM_LIMIT_LO_X,
                TView.DragMode.DM_LIMIT_LO_Y,
                TView.DragMode.DM_LIMIT_HI_X,
                TView.DragMode.DM_LIMIT_HI_Y), second);
    }

    @Test
    void dragModeMutatorsWork() {
        TestableTView view = new TestableTView(new TRect(0, 0, 1, 1));
        view.clearDragModes();
        assertTrue(view.getDragModes().isEmpty());
        view.addDragMode(TView.DragMode.DM_LIMIT_LO_X);
        view.addDragMode(TView.DragMode.DM_LIMIT_HI_Y);
        assertEquals(EnumSet.of(TView.DragMode.DM_LIMIT_LO_X,
                TView.DragMode.DM_LIMIT_HI_Y), view.getDragModes());
        view.removeDragMode(TView.DragMode.DM_LIMIT_LO_X);
        assertEquals(EnumSet.of(TView.DragMode.DM_LIMIT_HI_Y), view.getDragModes());
        view.setDragModes(TView.DragMode.limitAll());
        assertEquals(EnumSet.of(TView.DragMode.DM_LIMIT_LO_X,
                TView.DragMode.DM_LIMIT_LO_Y,
                TView.DragMode.DM_LIMIT_HI_X,
                TView.DragMode.DM_LIMIT_HI_Y), view.getDragModes());
    }

    @Test
    void eventAvailDetectsAndConsumesEvent() {
        queueView.events.add(keyPress(0));

        assertTrue(queueView.eventAvail());

        TEvent fetched = new TEvent();
        queueView.getEvent(fetched);
        assertEquals(TEvent.EV_KEYDOWN, fetched.what);
        queueView.clearEvent(fetched);

        assertFalse(queueView.eventAvail());
    }

    @Test
    void eventAvailReturnsTrueAndGetEventReturnsSameEvent() {
        queueView.putEvent(command(Command.CM_OK));

        assertTrue(queueView.eventAvail());

        TEvent fetched = new TEvent();
        queueView.getEvent(fetched);
        assertEquals(TEvent.EV_COMMAND, fetched.what);
    }

    @Test
    void clearEventResetsEvent() {
        TestableTView view = new TestableTView(new TRect(0, 0, 1, 1));
        TEvent event = keyPress(0);
        event.msg.infoPtr = new Object();

        view.clearEvent(event);

        assertEquals(TEvent.EV_NOTHING, event.what);
        assertSame(view, event.msg.infoPtr);
    }

    @Test
    void mouseEventStopsAtUpAndMatchesDown() {
        queueView.putEvent(mouseDown(0, 0));
        queueView.putEvent(mouseMove(0, 0));
        queueView.putEvent(mouseUp(0, 0));

        TEvent event = new TEvent();
        assertFalse(queueView.mouseEvent(event, TEvent.EV_MOUSE_MOVE));
        assertEquals(TEvent.EV_MOUSE_UP, event.what);

        assertTrue(queueView.mouseEvent(event, TEvent.EV_MOUSE_DOWN));
        assertEquals(TEvent.EV_MOUSE_DOWN, event.what);
    }

    @Test
    void dragViewMovesWithMouse() {
        TGroup parent = new TGroup(new TRect(0, 0, 10, 10));
        parent.insert(queueView);

        queueView.events.add(mouseMove(3, 3));
        queueView.events.add(mouseMove(7, 7));
        queueView.events.add(mouseUp(7, 7));

        TEvent down = mouseDown(0, 0);

        TRect limits = new TRect(0, 0, 5, 5);
        queueView.dragView(down, false);

        assertEquals(4, queueView.getOriginField().x);
        assertEquals(4, queueView.getOriginField().y);
    }

    @Test
    void dragViewMovesOriginRightWithKeyboard() {
        TGroup parent = new TGroup(new TRect(0, 0, 10, 10));
        parent.insert(queueView);

        queueView.events.add(keyPress(KeyCode.KB_RIGHT));
        queueView.events.add(keyPress(KeyCode.KB_ENTER));

        TRect limits = new TRect(0, 0, 10, 10);
        queueView.dragView(keyPress(0), false);

        assertEquals(1, queueView.getOriginField().x);
        assertEquals(0, queueView.getOriginField().y);
    }

    @Test
    void dragViewGrowClipsSizeWithinBounds() {
        EventQueueView view = new EventQueueView(new TRect(0, 0, 4, 4));
        TRect limits = new TRect(0, 0, 100, 100);
        TPoint minSize = new TPoint(2,2);
        TPoint maxSize = new TPoint(6,6);

        TEvent down = mouseDown(view.getSizeField().x, view.getSizeField().y);
        view.events.add(mouseMove(100, 100));
        view.events.add(mouseUp(0, 0));

        view.dragView(down, true);

        assertEquals(maxSize.x, view.getSizeField().x);
        assertEquals(maxSize.y, view.getSizeField().y);
    }

    @Test
    void dragViewKeepsOriginWithinLimitsWhenMovingRightRepeatedly() {
        TGroup parent = new TGroup(new TRect(0, 0, 10, 10));
        parent.insert(queueView);

        TRect limits = new TRect(0, 0, 3, 3);
        int maxX = limits.b.x - queueView.getSizeField().x;
        int maxY = limits.b.y - queueView.getSizeField().y;

        for (int i = 0; i < 5; i++) {
            queueView.events.add(keyPress(KeyCode.KB_RIGHT));
            queueView.events.add(keyPress(KeyCode.KB_ENTER));

            queueView.dragView(keyPress(0),
                    false);

            assertTrue(queueView.getOriginField().x >= limits.a.x);
            assertTrue(queueView.getOriginField().x <= maxX);
            assertTrue(queueView.getOriginField().y >= limits.a.y);
            assertTrue(queueView.getOriginField().y <= maxY);
        }

        assertEquals(maxX, queueView.getOriginField().x);
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
        TViewTestAccess.setOptions(view, OF_SELECTABLE);
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
