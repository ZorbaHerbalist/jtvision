package info.qbnet.jtvdemo;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TView;

import java.util.List;

public class DemoInterior extends TView {

    private final List<String> lines;

    public DemoInterior(TRect bounds, List<String> lines) {
        super(bounds);
        this.lines = lines;
        this.growMode |= (GrowMode.GF_GROW_HI_X + GrowMode.GF_GROW_HI_Y);
        this.options |= Options.OF_FRAMED;
    }

    @Override
    public void draw() {
        super.draw();
        int limit = Math.min(lines.size(), size.y);
        for (int y = 0; y < limit; y++) {
            writeStr(0, y, lines.get(y), getColor((short) 0x01));
        }
    }
}
