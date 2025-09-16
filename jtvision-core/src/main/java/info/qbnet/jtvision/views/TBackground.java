package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.*;

import java.io.IOException;
import java.util.EnumSet;

public class TBackground extends TView {

    public static final int CLASS_ID = 30;

    /**
     * Palette roles for {@link TBackground}.
     */
    public enum BackgroundColor implements PaletteRole {
        /** Background fill. */
        BACKGROUND(1, 0x01);

        private final int index;
        private final byte defaultValue;

        BackgroundColor(int index, int defaultValue) {
            this.index = index;
            this.defaultValue = PaletteRole.toByte(defaultValue);
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public byte defaultValue() {
            return defaultValue;
        }
    }

    public static void registerType() {
        TStream.registerType(CLASS_ID, TBackground::new);
    }

    private final char pattern;

    public static final TPalette C_BACKGROUND;

    static {
        PaletteFactory.registerDefaults("background", BackgroundColor.class);
        C_BACKGROUND = PaletteFactory.get("background");
    }

    public TBackground(TRect bounds, char pattern) {
        super(bounds);
        setGrowModes(EnumSet.of(GrowMode.GF_GROW_HI_X, GrowMode.GF_GROW_HI_Y));
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
