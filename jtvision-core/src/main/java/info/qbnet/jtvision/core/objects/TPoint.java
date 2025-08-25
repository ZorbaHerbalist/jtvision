package info.qbnet.jtvision.core.objects;

/**
 * TPoint is a simple object representing a point on the screen.
 * It is commonly used to describe coordinates on the screen or within UI components.
 */
public class TPoint {

    /**
     * X is the screen column of the point.
     */
    public int x;

    /**
     * Y is the screen row of the point.
     */
    public int y;

    /**
     * Constructs a point with the given coordinates.
     *
     * @param x screen column
     * @param y screen row
     */
    public TPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs a point at the origin (0, 0).
     */
    public TPoint() {
        this(0, 0);
    }

    @Override
    public String toString() {
        return "TPoint{" + "x=" + x + ", y=" + y + '}';
    }
}
