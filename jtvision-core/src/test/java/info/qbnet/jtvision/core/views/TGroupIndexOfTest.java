package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.core.objects.TRect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TGroupIndexOfTest {

    @Test
    void indexOfReturns1BasedPositionOrZero() {
        TGroup group = new TGroup(new TRect(0, 0, 10, 10));
        TView v1 = new TView(new TRect(0, 0, 1, 1));
        TView v2 = new TView(new TRect(0, 0, 1, 1));
        TView v3 = new TView(new TRect(0, 0, 1, 1));

        group.insert(v1);
        group.insert(v2);
        group.insert(v3);

        assertEquals(1, group.indexOf(v3));
        assertEquals(2, group.indexOf(v2));
        assertEquals(3, group.indexOf(v1));
        assertEquals(0, group.indexOf(new TView(new TRect(0, 0, 1, 1))));

        TGroup empty = new TGroup(new TRect(0, 0, 5, 5));
        assertEquals(0, empty.indexOf(v1));
        assertEquals(0, group.indexOf(null));
    }
}
