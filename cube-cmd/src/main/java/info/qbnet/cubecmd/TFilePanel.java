package info.qbnet.cubecmd;

import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TScrollBar;

public class TFilePanel extends TFilePanelRoot {

    public TFilePanel(TRect bounds, int drive, TScrollBar scrollBar) {
        super(bounds, drive, scrollBar);
    }

    @Override
    public void draw() {
        // TODO
        TDrawBuffer buf = new TDrawBuffer();
        buf.moveChar(0,'w', getColor((short) 1), size.x);
        writeLine(0,0, size.x, size.y, buf.buffer);
    }
}
