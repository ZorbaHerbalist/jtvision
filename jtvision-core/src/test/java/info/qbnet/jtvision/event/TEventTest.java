package info.qbnet.jtvision.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static info.qbnet.jtvision.event.TestFixtures.keyPress;

class TEventTest {

    @Test
    void copyFromCopiesFields() {
        TEvent source = keyPress(123);
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
