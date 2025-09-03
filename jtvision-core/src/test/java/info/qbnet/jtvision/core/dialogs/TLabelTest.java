package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TView;
import info.qbnet.jtvision.core.views.support.TestableTView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TLabelTest {

    static class FocusableView extends TestableTView {
        FocusableView(TRect bounds) {
            super(bounds);
            options |= Options.OF_SELECTABLE;
        }
        @Override
        public boolean focus() {
            setState(State.SF_FOCUSED, true);
            return true;
        }
    }

    @Test
    void mouseDownFocusesLinkedView() {
        FocusableView target = new FocusableView(new TRect(0, 0, 1, 1));
        TLabel label = new TLabel(new TRect(0, 0, 5, 1), "~N~ame", target);
        TEvent e = new TEvent();
        e.what = TEvent.EV_MOUSE_DOWN;
        label.handleEvent(e);
        assertTrue((target.state & TView.State.SF_FOCUSED) != 0);
        assertEquals(TEvent.EV_NOTHING, e.what);
    }
}
