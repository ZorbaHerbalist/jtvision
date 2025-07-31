package info.qbnet.core.objects;

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

}
