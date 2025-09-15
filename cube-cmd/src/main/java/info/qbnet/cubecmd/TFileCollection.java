package info.qbnet.cubecmd;

import java.util.ArrayList;
import java.util.List;

public class TFileCollection {

    private final List<TFileRec> items = new ArrayList<>();

    public TFileCollection() {
    }

    public void add(TFileRec fileRec) {
        items.add(fileRec);
    }

    public TFileRec get(int idx) {
        return items.get(idx);
    }

    public int size() {
        return items.size();
    }

}
