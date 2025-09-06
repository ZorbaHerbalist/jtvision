package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;
import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.TPalette;

import java.io.IOException;
import java.util.EnumSet;

import static info.qbnet.jtvision.util.TPalette.parseHexString;

public class TBackground extends TView {

    public static final int CLASS_ID = 30;

    public static void registerType() {
        TStream.registerType(CLASS_ID, TBackground::new);
    }

    private final char pattern;

    public static final TPalette C_BACKGROUND = new TPalette(parseHexString("\\x01"));

    public TBackground(TRect bounds, char pattern) {
        super(bounds);
        getGrowMode().addAll(EnumSet.of(GrowMode.GF_GROW_HI_X, GrowMode.GF_GROW_HI_Y));
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
