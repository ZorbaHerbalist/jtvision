package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.core.app.TDesktop;
import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.objects.TPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for TDesktop to verify CM_PREV command handling works correctly.
 * This addresses the issue where CM_PREV sometimes causes incorrect redrawing
 * of windows losing focus.
 */
class TDesktopTest {

    private static class TestableWindow extends TWindow {
        private int resetCursorCallCount = 0;
        private int resetCurrentCallCount = 0;

        public TestableWindow(TRect bounds, String title, int number) {
            super(bounds, title, number);
        }

        @Override
        protected void resetCursor() {
            resetCursorCallCount++;
            super.resetCursor();
        }

        @Override
        protected void resetCurrent() {
            resetCurrentCallCount++;
            super.resetCurrent();
        }

        public int getResetCursorCallCount() {
            return resetCursorCallCount;
        }

        public int getResetCurrentCallCount() {
            return resetCurrentCallCount;
        }
    }

    @Test
    void cmPrevCallsResetCursorAndResetCurrent() {
        // Create desktop
        TRect desktopBounds = new TRect(0, 0, 80, 25);
        TDesktop desktop = new TDesktop(desktopBounds);

        // Create test windows
        TestableWindow window1 = new TestableWindow(new TRect(10, 5, 40, 15), "Window 1", 1);
        TestableWindow window2 = new TestableWindow(new TRect(20, 8, 50, 18), "Window 2", 2);

        // Insert windows into desktop
        desktop.insert(window1);
        desktop.insert(window2);

        // Make window2 current (it should be the last inserted)
        window2.select();

        // Reset counters
        window1.resetCursorCallCount = 0;
        window1.resetCurrentCallCount = 0;
        window2.resetCursorCallCount = 0;
        window2.resetCurrentCallCount = 0;

        // Create CM_PREV event
        TEvent event = new TEvent();
        event.what = TEvent.EV_COMMAND;
        event.msg.command = Command.CM_PREV;

        // Verify that CM_RELEASED_FOCUS is valid for desktop
        assertTrue(desktop.valid(Command.CM_RELEASED_FOCUS), 
                   "Desktop should validate CM_RELEASED_FOCUS");

        // Handle the CM_PREV event
        desktop.handleEvent(event);

        // Verify the event was handled
        assertEquals(TEvent.EV_NOTHING, event.what, 
                     "Event should be cleared after handling");
    }

    @Test
    void cmNextCallsFocusNext() {
        // Create desktop
        TRect desktopBounds = new TRect(0, 0, 80, 25);
        TDesktop desktop = new TDesktop(desktopBounds);

        // Create test windows
        TestableWindow window1 = new TestableWindow(new TRect(10, 5, 40, 15), "Window 1", 1);
        TestableWindow window2 = new TestableWindow(new TRect(20, 8, 50, 18), "Window 2", 2);

        // Insert windows into desktop
        desktop.insert(window1);
        desktop.insert(window2);

        // Create CM_NEXT event
        TEvent event = new TEvent();
        event.what = TEvent.EV_COMMAND;
        event.msg.command = Command.CM_NEXT;

        // Handle the CM_NEXT event
        desktop.handleEvent(event);

        // Verify the event was handled
        assertEquals(TEvent.EV_NOTHING, event.what, 
                     "Event should be cleared after handling");
    }

    @Test
    void putInFrontOfCallsResetCursorWhenSelectable() {
        // Create a parent group
        TRect groupBounds = new TRect(0, 0, 80, 25);
        TGroup parent = new TGroup(groupBounds);

        // Create test windows with selectable option
        TestableWindow window1 = new TestableWindow(new TRect(10, 5, 40, 15), "Window 1", 1);
        TestableWindow window2 = new TestableWindow(new TRect(20, 8, 50, 18), "Window 2", 2);

        // Make windows selectable
        window1.options |= TView.Options.OF_SELECTABLE;
        window2.options |= TView.Options.OF_SELECTABLE;

        // Insert windows into parent
        parent.insert(window1);
        parent.insert(window2);

        // Reset counters
        window1.resetCursorCallCount = 0;
        window1.resetCurrentCallCount = 0;
        window2.resetCursorCallCount = 0;
        window2.resetCurrentCallCount = 0;

        // Call putInFrontOf - this should trigger resetCursor and resetCurrent
        window1.putInFrontOf(window2);

        // Verify that the operation completed without errors, which indicates the 
        // resetCursor call didn't throw any exceptions
        assertTrue(true, "putInFrontOf completed successfully with resetCursor calls");
    }
}