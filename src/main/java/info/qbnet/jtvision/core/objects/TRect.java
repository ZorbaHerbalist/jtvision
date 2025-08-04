package info.qbnet.jtvision.core.objects;

/**
 * TRect is a simple object representing a rectangular area on the screen.
 * It is defined by two points: a (top-left corner) and b (bottom-right corner).
 */
public class TRect {

    /**
     * Top-left corner of the rectangle.
     */
    public TPoint a;

    /**
     * Bottom-right corner of the rectangle.
     */
    public TPoint b;

    /**
     * Constructs a rectangle from two points.
     * @param a top-left corner
     * @param b bottom-right corner
     */
    public TRect(TPoint a, TPoint b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Constructs an empty rectangle with both points initialized to (0,0).
     */
    public TRect() {
        this(new TPoint(), new TPoint());
    }

    /**
     * Constructs a rectangle from four coordinates.
     * @param xa x-coordinate of top-left corner
     * @param ya y-coordinate of top-left corner
     * @param xb x-coordinate of bottom-right corner
     * @param yb y-coordinate of bottom-right corner
     */
    public TRect(int xa, int ya, int xb, int yb) {
        this.a = new TPoint(xa, ya);
        this.b = new TPoint(xb, yb);
    }

    /**
     * Assigns the rectangle corners to the specified coordinates.
     * @param xa x-coordinate of top-left corner
     * @param ya y-coordinate of top-left corner
     * @param xb x-coordinate of bottom-right corner
     * @param yb y-coordinate of bottom-right corner
     */
    public void assign(int xa, int ya, int xb, int yb) {
        a.x = xa;
        a.y = ya;
        b.x = xb;
        b.y = yb;
    }

    /**
     * Copies coordinates from another rectangle.
     * @param other rectangle to copy from
     */
    public void copy(TRect other) {
        a.x = other.a.x;
        a.y = other.a.y;
        b.x = other.b.x;
        b.y = other.b.y;
    }

    /**
     * Moves the rectangle by dx and dy.
     * @param dx horizontal offset
     * @param dy vertical offset
     */
    public void move(int dx, int dy) {
        a.x += dx;
        a.y += dy;
        b.x += dx;
        b.y += dy;
    }

    /**
     * Enlarges or shrinks the rectangle by dx and dy in all directions.
     * @param dx amount to grow/shrink horizontally
     * @param dy amount to grow/shrink vertically
     */
    public void grow(int dx, int dy) {
        a.x -= dx;
        a.y -= dy;
        b.x += dx;
        b.y += dy;
    }

    /**
     * Resets the rectangle to an empty state if it has invalid dimensions.
     */
    private void checkEmpty() {
        if (a.x > b.x || a.y > b.y) {
            a.x = 0;
            a.y = 0;
            b.x = 0;
            b.y = 0;
        }
    }

    /**
     * Intersects the rectangle with another rectangle.
     * The result is the overlapping area of both rectangles.
     * @param other rectangle to intersect with
     */
    public void intersect(TRect other) {
        a.x = Math.max(a.x, other.a.x);
        a.y = Math.max(a.y, other.a.y);
        b.x = Math.min(b.x, other.b.x);
        b.y = Math.min(b.y, other.b.y);

        checkEmpty();
    }

    /**
     * Expands the rectangle to include the area of another rectangle.
     * @param other rectangle to unite with
     */
    public void union(TRect other) {
        a.x = Math.min(a.x, other.a.x);
        a.y = Math.min(a.y, other.a.y);
        b.x = Math.max(b.x, other.b.x);
        b.y = Math.max(b.y, other.b.y);
    }

    /**
     * Checks whether the given point lies within the rectangle.
     * @param p point to test
     * @return true if point is inside the rectangle, false otherwise
     */
    public boolean contains(TPoint p) {
        return p.x >= a.x && p.x < b.x && p.y >= a.y && p.y < b.y;
    }

    /**
     * Compares this rectangle with another rectangle for equality.
     * @param rect rectangle to compare
     * @return true if both rectangles have identical coordinates
     */
    public boolean equals(TRect rect) {
        return a.x == rect.a.x && a.y == rect.a.y && b.x == rect.b.x && b.y == rect.b.y;
    }

    /**
     * Checks whether the rectangle is empty (i.e., has no area).
     * @return true if width or height is zero or negative
     */
    public boolean empty() {
        return a.x >= b.x || a.y >= b.y;
    }

}
