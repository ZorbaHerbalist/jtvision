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
        BACKGROUND;
    }

    public static void registerType() {
        TStream.registerType(CLASS_ID, TBackground::new);
    }

    private final char pattern;

    public static final PaletteDescriptor<BackgroundColor> BACKGROUND_PALETTE =
            PaletteDescriptor.register("background", BackgroundColor.class);

    public TBackground(TRect bounds, char pattern) {
        super(bounds);
        setGrowModes(EnumSet.of(GrowMode.HI_X, GrowMode.HI_Y));
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
        buf.moveChar(0, pattern, getColor(BackgroundColor.BACKGROUND), getSize().x);
        writeLine(0,0, getSize().x, getSize().y, buf.buffer);

        logger.trace("{} Background color {}", getLogName(), getColor(BackgroundColor.BACKGROUND));
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
        return BACKGROUND_PALETTE.palette();
    }
}
