package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.support.DataView;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class TGroupDataTest {

    @Test
    void dataSizeIncludesSubviews() {
        TGroup root = new TGroup(new TRect(0, 0, 10, 10));
        TGroup child = new TGroup(new TRect(0, 0, 1, 1));
        DataView top = new DataView(new TRect(0, 0, 1, 1), new byte[]{1, 2});
        DataView nested = new DataView(new TRect(0, 0, 1, 1), new byte[]{3, 4, 5});

        child.insert(nested);
        root.insert(child);
        root.insert(top);

        assertEquals(3, child.dataSize());
        assertEquals(5, root.dataSize());
    }

    @Test
    void getDataConcatenatesSubviewDataInOrder() {
        TGroup root = new TGroup(new TRect(0, 0, 10, 10));
        TGroup child = new TGroup(new TRect(0, 0, 1, 1));
        DataView top = new DataView(new TRect(0, 0, 1, 1), new byte[]{1, 2});
        DataView nested = new DataView(new TRect(0, 0, 1, 1), new byte[]{3, 4, 5});

        child.insert(nested);
        root.insert(child);
        root.insert(top);

        ByteBuffer dst = ByteBuffer.allocate(root.dataSize());
        root.getData(dst);

        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, dst.array());
        assertEquals(root.dataSize(), dst.position());
    }

    @Test
    void setDataDistributesBytesToSubviews() {
        TGroup root = new TGroup(new TRect(0, 0, 10, 10));
        TGroup child = new TGroup(new TRect(0, 0, 1, 1));
        DataView top = new DataView(new TRect(0, 0, 1, 1), new byte[2]);
        DataView nested = new DataView(new TRect(0, 0, 1, 1), new byte[3]);

        child.insert(nested);
        root.insert(child);
        root.insert(top);

        ByteBuffer src = ByteBuffer.wrap(new byte[]{5, 6, 7, 8, 9});
        root.setData(src);

        assertArrayEquals(new byte[]{5, 6}, top.getBytes());
        assertArrayEquals(new byte[]{7, 8, 9}, nested.getBytes());
        assertEquals(root.dataSize(), src.position());
    }
}
