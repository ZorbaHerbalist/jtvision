package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;

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

    @Test
    void loadingDisablesButtonIfCommandInactive() throws Exception {
        TButton b = new TButton(new TRect(0, 0, 10, 2), "Test", Command.CM_HELP, TButton.BF_NORMAL);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TStream outStream = new TStream(out);
        outStream.storeView(b);

        byte[] data = out.toByteArray();

        TView.disableCommands(Set.of(Command.CM_HELP));
        try {
            TStream inStream = new TStream(new ByteArrayInputStream(data));
            TView loaded = inStream.loadView();
            assertTrue(loaded instanceof TButton);
            assertTrue((loaded.state & TView.State.SF_DISABLED) != 0);
        } finally {
            TView.enableCommands(Set.of(Command.CM_HELP));
        }
    }
}
