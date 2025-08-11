package info.qbnet.jtvision.core.menus;

import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TDrawBuffer;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;
import info.qbnet.jtvision.util.CString;

public class TStatusLine extends TView {

    private TStatusDef defs;
    private TStatusItem items;

    public static final TPalette C_STATUS_LINE = new TPalette(TPalette.parseHexString("\\x02\\x03\\x04\\x05\\x06\\x07"));

    public TStatusLine(TRect bounds, TStatusDef defs) {
        super(bounds);
        this.options |= Options.OF_PRE_PROCESS;
        this.eventMask |= TEvent.EV_BROADCAST;
        this.growMode = GrowMode.GF_GROW_LO_Y | GrowMode.GF_GROW_HI_X | GrowMode.GF_GROW_HI_Y;
        this.defs = defs;
        findItems();

        logger.debug("{} TStatusLine@TStatusLine(bounds={}, defs={})", getLogName(), bounds, defs);
    }

    @Override
    public void draw() {
        logger.trace("{} TStatusLine@draw()", getLogName());

        drawSelect(null);
    }

    private void drawSelect(TStatusDef selected) {
        TDrawBuffer buf = new TDrawBuffer();

        short cNormal = getColor((short) 0x0301);
        short cSelect = getColor((short) 0x0604);
        short cNormDisabled = getColor((short) 0x0202);
        short cSelDisabled = getColor((short) 0x0505);

        buf.moveChar(0, ' ', cNormal, size.x);
        TStatusItem t = items;
        int i = 0;
        while (t != null) {
            logger.trace("{} TStatusLine@drawSelect() item {}", getLogName(), t);
            if (t.text() != null) {
                int l = CString.cStrLen(t.text());
                if (i + l < size.x) {
                    // TODO
                    short color = cNormal;
                    buf.moveChar(i, ' ' , (char) color, 1);
                    buf.moveCStr(i + 1, t.text(), color);
                    buf.moveChar(i + l + 1, ' ', (char) color, 1);
                }
                i = i + l + 2;
            }
            t = t.next();
        }
        // TODO

        writeLine(0, 0, size.x, 1, buf.buffer);
    }

    private void findItems() {
        // TODO
        if (defs != null) {
            items = defs.items();
        } else {
            items = null;
        }
    }

    @Override
    public TPalette getPalette() {
        return C_STATUS_LINE;
    }
}
