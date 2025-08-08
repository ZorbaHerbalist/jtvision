package info.qbnet.jtvision.core.app;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TDrawBuffer;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;

import static info.qbnet.jtvision.core.views.TPalette.parseHexString;

public class TBackground extends TView {

    private final char pattern;

    public static final TPalette C_BACKGROUND = new TPalette(parseHexString("\\x01"));

    public TBackground(TRect bounds, char pattern) {
        super(bounds);
        this.pattern = pattern;

        logger.debug("{} TBackground@TBackground(bounds={}, pattern={})", getLogName(), bounds, (int) pattern);
    }

    @Override
    public void draw() {
        logger.trace("{} TBackground@draw()", getLogName());

        TDrawBuffer buf = new TDrawBuffer();
        buf.moveChar(0, pattern, getColor((short) 1), size.x);
        writeLine(0,0, size.x, size.y, buf.buffer);
    }

    @Override
    public TPalette getPalette() {
        return C_BACKGROUND;
    }
}
