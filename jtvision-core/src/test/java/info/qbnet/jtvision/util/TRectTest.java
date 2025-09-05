package info.qbnet.jtvision.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TRectTest {

    @Test
    void constructorWithPoints() {
        TPoint a = new TPoint(1, 2);
        TPoint b = new TPoint(3, 4);
        TRect rect = new TRect(a, b);
        assertEquals(1, rect.a.x);
        assertEquals(2, rect.a.y);
        assertEquals(3, rect.b.x);
        assertEquals(4, rect.b.y);
    }

    @Test
    void defaultConstructor() {
        TRect rect = new TRect();
        assertEquals(0, rect.a.x);
        assertEquals(0, rect.a.y);
        assertEquals(0, rect.b.x);
        assertEquals(0, rect.b.y);
    }

    @Test
    void constructorWithCoordinates() {
        TRect rect = new TRect(1, 2, 3, 4);
        assertEquals(1, rect.a.x);
        assertEquals(2, rect.a.y);
        assertEquals(3, rect.b.x);
        assertEquals(4, rect.b.y);
    }

    @Test
    void assignCopiesCoordinates() {
        TRect rect = new TRect();
        rect.assign(1, 2, 3, 4);
        assertEquals(1, rect.a.x);
        assertEquals(2, rect.a.y);
        assertEquals(3, rect.b.x);
        assertEquals(4, rect.b.y);
    }

    @Test
    void copyDuplicatesCoordinates() {
        TRect rect1 = new TRect(1, 2, 3, 4);
        TRect rect2 = new TRect();
        rect2.copy(rect1);
        assertTrue(rect1.equals(rect2));
    }

    @Test
    void moveShiftsRectangle() {
        TRect rect = new TRect(1, 2, 3, 4);
        rect.move(2, 3);
        assertEquals(3, rect.a.x);
        assertEquals(5, rect.a.y);
        assertEquals(5, rect.b.x);
        assertEquals(7, rect.b.y);
    }

    @Test
    void growResizesRectangle() {
        TRect rect = new TRect(2, 2, 4, 4);
        rect.grow(1, 2);
        assertEquals(1, rect.a.x);
        assertEquals(0, rect.a.y);
        assertEquals(5, rect.b.x);
        assertEquals(6, rect.b.y);
    }

    @Test
    void intersectProducesOverlap() {
        TRect rect1 = new TRect(0, 0, 5, 5);
        TRect rect2 = new TRect(3, 3, 8, 8);
        rect1.intersect(rect2);
        assertEquals(3, rect1.a.x);
        assertEquals(3, rect1.a.y);
        assertEquals(5, rect1.b.x);
        assertEquals(5, rect1.b.y);
    }

    @Test
    void intersectNonOverlappingClearsRectangle() {
        TRect rect1 = new TRect(0, 0, 2, 2);
        TRect rect2 = new TRect(3, 3, 5, 5);
        rect1.intersect(rect2);
        assertTrue(rect1.empty());
        assertEquals(0, rect1.a.x);
        assertEquals(0, rect1.a.y);
        assertEquals(0, rect1.b.x);
        assertEquals(0, rect1.b.y);
    }

    @Test
    void unionProducesBoundingRectangle() {
        TRect rect1 = new TRect(0, 0, 2, 2);
        TRect rect2 = new TRect(3, 3, 5, 5);
        rect1.union(rect2);
        assertEquals(0, rect1.a.x);
        assertEquals(0, rect1.a.y);
        assertEquals(5, rect1.b.x);
        assertEquals(5, rect1.b.y);
    }

    @Test
    void containsRespectsBounds() {
        TRect rect = new TRect(0, 0, 10, 10);
        assertTrue(rect.contains(new TPoint(0, 0)));
        assertTrue(rect.contains(new TPoint(9, 9)));
        assertFalse(rect.contains(new TPoint(10, 0)));
        assertFalse(rect.contains(new TPoint(0, 10)));
    }

    @Test
    void equalsAndEmptyWork() {
        TRect rect1 = new TRect(0, 0, 5, 5);
        TRect rect2 = new TRect(0, 0, 5, 5);
        TRect rect3 = new TRect(0, 0, 0, 5);
        assertTrue(rect1.equals(rect2));
        assertFalse(rect1.equals(rect3));
        assertTrue(rect3.empty());
        assertFalse(rect1.empty());
    }
}

