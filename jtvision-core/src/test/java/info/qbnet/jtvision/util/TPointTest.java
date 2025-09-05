package info.qbnet.jtvision.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TPointTest {

    @Test
    void constructorWithCoordinates() {
        TPoint point = new TPoint(5, 10);
        assertEquals(5, point.x);
        assertEquals(10, point.y);
    }

    @Test
    void defaultConstructor() {
        TPoint point = new TPoint();
        assertEquals(0, point.x);
        assertEquals(0, point.y);
    }
}
