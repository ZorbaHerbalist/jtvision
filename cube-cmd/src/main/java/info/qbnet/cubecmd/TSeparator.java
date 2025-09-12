package info.qbnet.cubecmd;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.TPalette;
import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TRect;

public class TSeparator extends THideView {

    // Palette layout
    // 1 = Passive frame
    // 2 = Active frame
    // 3 = Dragging
    public static final TPalette C_SEPARATOR = new TPalette(TPalette.parseHexString("\\x01\\x02\\x03"));

    private static final String FRAME_CHARS = "\u00BB\u00BC\u00C9\u00C8\u00BA\u00BF\u00D9\u00DA\u00C0\u00B3";

    private int oldX;
    private int oldW;

    public TSeparator(TRect bounds, int width) {
        super(bounds);
        oldX = origin.x + 1;
        oldW = width;
        eventMask = 0xFFFF;
    }

    @Override
    public void draw() {
        TDrawBuffer buf = new TDrawBuffer();

        short color;
        int off = 0;

        if ((state & State.SF_DRAGGING) != 0) {
            color = getColor((short) 0x03);
            off = 5;
        } else if ((state & State.SF_ACTIVE) == 0) {
            color = getColor((short) 0x01);
            off = 5;
        } else {
            color = getColor((short) 0x02);
        }

        buf.moveChar(0, FRAME_CHARS.charAt(off), color, 1);
        buf.moveChar(1, FRAME_CHARS.charAt(2 + off), color, 1);
        buf.moveChar(2, FRAME_CHARS.charAt(4 + off), color, (size.y - 2) * 2);
        buf.moveChar(size.y * 2 - 2, FRAME_CHARS.charAt(1 + off), color, 1);
        buf.moveChar(size.y * 2 - 1, FRAME_CHARS.charAt(3 + off), color, 1);
        writeBuf(0, 0, 2, size.y, buf.buffer);
    }

    @Override
    public TPalette getPalette() {
        return C_SEPARATOR;
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        // TODO
//        if (event.what == TEvent.EV_MOUSE_DOWN) {
//            TPoint mouse = new TPoint();
//            makeLocal(event.mouse.where, mouse);
//            do {
//                makeLocal(event.mouse.where, mouse);
//                if (mouse.x >= 1 && mouse.x < owner.size.x - 2) {
//
//                }
//
//            } while (mouseEvent(event, TEvent.EV_MOUSE_MOVE));
//            clearEvent(event);
//        }
    }
}
