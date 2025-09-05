package info.qbnet.jtvision.views.support;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TGroup;

import java.nio.ByteBuffer;

/**
 * Simple {@link TGroup} subclass that owns its own byte array and participates
 * in the {@code dataSize}, {@code getData} and {@code setData} contract.
 */
public class DataGroup extends TGroup {
    private final byte[] data;

    public DataGroup(TRect bounds, byte[] data) {
        super(bounds);
        this.data = data;
    }

    @Override
    public int dataSize() {
        return super.dataSize() + data.length;
    }

    @Override
    public void getData(ByteBuffer dst) {
        dst.put(data);
        super.getData(dst);
    }

    @Override
    public void setData(ByteBuffer src) {
        src.get(data);
        super.setData(src);
    }

    public byte[] getBytes() {
        return data;
    }
}
