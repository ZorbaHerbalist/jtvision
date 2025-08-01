package info.qbnet.jtvision.core.objects;

public class TRect {

    public TPoint a;
    public TPoint b;

    public TRect(TPoint a, TPoint b) {
        this.a = a;
        this.b = b;
    }

    public TRect() {
        this(new TPoint(), new TPoint());
    }

    public TRect(int x1, int y1, int x2, int y2) {
        this(new TPoint(x1, y1), new TPoint(x2, y2));
    }

}
