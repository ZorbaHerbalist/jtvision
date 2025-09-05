package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.support.DataView;
import info.qbnet.jtvision.views.support.DataGroup;
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

    @Test
    void dataSizeAccountsForGroupOwnData() {
        DataGroup root = new DataGroup(new TRect(0, 0, 10, 10), new byte[]{10, 11});
        DataView v1 = new DataView(new TRect(0, 0, 1, 1), new byte[]{1});
        DataGroup child = new DataGroup(new TRect(0, 0, 1, 1), new byte[]{20, 21, 22});
        DataView v2 = new DataView(new TRect(0, 0, 1, 1), new byte[]{2, 3});

        child.insert(v2);
        root.insert(v1);
        root.insert(child);

        assertEquals(5, child.dataSize());
        assertEquals(8, root.dataSize());
    }

    @Test
    void getDataIncludesGroupBytesAndMaintainsOrder() {
        DataGroup root = new DataGroup(new TRect(0, 0, 10, 10), new byte[]{10, 11});
        DataView v1 = new DataView(new TRect(0, 0, 1, 1), new byte[]{1});
        DataGroup child = new DataGroup(new TRect(0, 0, 1, 1), new byte[]{20, 21, 22});
        DataView v2 = new DataView(new TRect(0, 0, 1, 1), new byte[]{2, 3});

        child.insert(v2);
        root.insert(v1);
        root.insert(child);

        ByteBuffer dst = ByteBuffer.allocate(root.dataSize());
        root.getData(dst);

        assertArrayEquals(new byte[]{10, 11, 20, 21, 22, 2, 3, 1}, dst.array());
        assertEquals(root.dataSize(), dst.position());
    }

    @Test
    void setDataWritesToGroupAndSubviewsInOrder() {
        DataGroup root = new DataGroup(new TRect(0, 0, 10, 10), new byte[2]);
        DataView v1 = new DataView(new TRect(0, 0, 1, 1), new byte[1]);
        DataGroup child = new DataGroup(new TRect(0, 0, 1, 1), new byte[3]);
        DataView v2 = new DataView(new TRect(0, 0, 1, 1), new byte[2]);

        child.insert(v2);
        root.insert(v1);
        root.insert(child);

        ByteBuffer src = ByteBuffer.wrap(new byte[]{50, 51, 60, 61, 62, 70, 71, 80});
        root.setData(src);

        assertArrayEquals(new byte[]{50, 51}, root.getBytes());
        assertArrayEquals(new byte[]{60, 61, 62}, child.getBytes());
        assertArrayEquals(new byte[]{70, 71}, v2.getBytes());
        assertArrayEquals(new byte[]{80}, v1.getBytes());
        assertEquals(root.dataSize(), src.position());
    }
}
