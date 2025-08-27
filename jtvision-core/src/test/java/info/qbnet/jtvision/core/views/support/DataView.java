package info.qbnet.jtvision.core.views.support;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TView;

import java.nio.ByteBuffer;

public class DataView extends TView {
    private final byte[] data;

    public DataView(TRect bounds, byte[] data) {
        super(bounds);
        this.data = data;
    }

    @Override
    public void draw() {
        // no-op
    }

    @Override
    public int dataSize() {
        return data.length;
    }

    @Override
    public void getData(ByteBuffer dst) {
        dst.put(data);
    }

    @Override
    public void setData(ByteBuffer src) {
        src.get(data);
    }

    public byte[] getBytes() {
        return data;
    }
}
