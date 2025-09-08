package info.qbnet.jtvdemo;

import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TScrollBar;
import info.qbnet.jtvision.views.TScroller;
import info.qbnet.jtvision.views.TView;

import java.util.EnumSet;
import java.util.List;

public class DemoInterior extends TScroller {

    private final List<String> lines;

    public DemoInterior(TRect bounds, TScrollBar hScrollBar, TScrollBar vScrollBar, List<String> lines) {
        super(bounds, hScrollBar, vScrollBar);
        this.lines = lines;
        this.getGrowMode().addAll(EnumSet.of(GrowMode.GF_GROW_HI_X, GrowMode.GF_GROW_HI_Y));
        this.options |= Options.OF_FRAMED;
        setLimit(128, lines.size());
    }

    @Override
    public void draw() {
//        short color = getColor((short) 0x01);
//        TDrawBuffer b = new TDrawBuffer();
//        for (int y = 0; y < size.y) {
//            b.moveChar(0, ' ', color, size.x);
//            int i = delta.y + y;
//            if (i < lines.size() && lines.get(i) != null) {
//                b.moveStr(0, lines.get(i), delta.x + 1, size.x, color);
//            }
//        }

        super.draw();
        int limit = Math.min(lines.size(), size.y);
        for (int y = 0; y < limit; y++) {
            writeStr(0, y, lines.get(y), getColor((short) 0x01));
        }
    }
}
