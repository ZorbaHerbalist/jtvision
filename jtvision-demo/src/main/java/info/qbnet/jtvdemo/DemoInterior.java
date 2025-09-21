package info.qbnet.jtvdemo;

import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TScrollBar;
import info.qbnet.jtvision.views.TScroller;

import java.util.EnumSet;
import java.util.List;

public class DemoInterior extends TScroller {

    private final List<String> lines;

    public DemoInterior(TRect bounds, TScrollBar hScrollBar, TScrollBar vScrollBar, List<String> lines) {
        super(bounds, hScrollBar, vScrollBar);
        this.lines = lines;
        setGrowModes(EnumSet.of(GrowMode.GF_GROW_HI_X, GrowMode.GF_GROW_HI_Y));
        this.options |= Options.OF_FRAMED;
        setLimit(128, lines.size());
    }

    @Override
    public void draw() {
        short color = getColor(ScrollerColor.NORMAL_TEXT);
        TDrawBuffer b = new TDrawBuffer();
        for (int y = 0; y < getSize().y; y++) {
            b.moveChar(0, ' ', color, getSize().x);
            int i = delta.y + y;
            if (i < lines.size()) {
                String line = lines.get(i);
                if (line.length() > delta.x) {
                    String part = line.substring(delta.x, Math.min(line.length(), delta.x + getSize().x));
                    b.moveStr(0, part, color);
                }
            }
            writeLine(0, y, getSize().x, 1, b.buffer);
        }
    }
}
