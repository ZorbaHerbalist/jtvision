package info.qbnet.jtvision.core.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TEventTest {

    @Test
    void copyFromCopiesFields() {
        TEvent source = new TEvent();
        source.what = TEvent.EV_KEYDOWN;
        source.key.keyCode = 123;
        source.key.charCode = 'x';
        source.key.scanCode = 42;

        TEvent target = new TEvent();
        target.copyFrom(source);

        assertEquals(source.what, target.what);
        assertEquals(source.key.keyCode, target.key.keyCode);
        assertEquals(source.key.charCode, target.key.charCode);
        assertEquals(source.key.scanCode, target.key.scanCode);
    }
}
