package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TButtonTest {

    static class TestButton extends TButton {
        boolean pressed = false;
        TestButton(TRect bounds) {
            super(bounds, "Test", 300, TButton.BF_NORMAL);
        }
        @Override
        public void press() {
            pressed = true;
        }
    }

    @Test
    void spaceKeyPressActivatesButton() {
        TestButton b = new TestButton(new TRect(0, 0, 10, 2));
        b.setState(TView.State.SF_FOCUSED, true);
        TEvent e = new TEvent();
        e.what = TEvent.EV_KEYDOWN;
        e.key.charCode = ' ';
        b.handleEvent(e);
        assertTrue(b.pressed);
    }
}
