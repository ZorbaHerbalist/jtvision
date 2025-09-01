package info.qbnet.jtvision.core.app;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.objects.TStream;
import info.qbnet.jtvision.core.views.TDrawBuffer;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;

import java.io.IOException;

import static info.qbnet.jtvision.core.views.TPalette.parseHexString;

public class TBackground extends TView {

    /** Serialization identifier for {@code TBackground} views. */
    public static final int CLASS_ID = 9;

    static {
        TStream.registerType(CLASS_ID, TBackground::new);
    }

    private final char pattern;

    public static final TPalette C_BACKGROUND = new TPalette(parseHexString("\\x01"));

    public TBackground(TRect bounds, char pattern) {
        super(bounds);
        this.growMode |= (GrowMode.GF_GROW_HI_X + GrowMode.GF_GROW_HI_Y);
        this.pattern = pattern;

        logger.debug("{} TBackground@TBackground(bounds={}, pattern={})", getLogName(), bounds, (int) pattern);
    }

    public TBackground(TStream stream) {
        super(stream);
        try {
            this.pattern = (char) stream.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void draw() {
        logger.trace("{} TBackground@draw()", getLogName());

        TDrawBuffer buf = new TDrawBuffer();
        buf.moveChar(0, pattern, getColor((short) 1), size.x);
        writeLine(0,0, size.x, size.y, buf.buffer);

        logger.trace("{} Background color {}", getLogName(), getColor((short) 1));
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            stream.writeInt(pattern);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TPalette getPalette() {
        return C_BACKGROUND;
    }
}
