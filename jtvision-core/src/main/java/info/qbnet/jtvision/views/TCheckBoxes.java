package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;

import java.util.List;

/**
 * A group of independent toggleable options represented as check boxes.
 * <p>
 * Each option corresponds to a bit in {@link #value}; toggling a box flips the
 * associated bit allowing multiple selections to be active simultaneously.
 * </p>
 */
public class TCheckBoxes extends TCluster {

    public static final int CLASS_ID = 15;

    /** Registers this class for stream persistence. */
    public static void registerType() {
        TStream.registerType(CLASS_ID, TCheckBoxes::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    public TCheckBoxes(TRect bounds, List<String> strings) {
        super(bounds, strings);
    }

    public TCheckBoxes(TStream stream) {
        super(stream);
    }

    @Override
    public void draw() {
        drawMultiBox(" [ ] ", " X");
    }

    @Override
    protected boolean mark(int item) {
        return (value & (1 << item)) != 0;
    }

    @Override
    protected void press(int item) {
        value ^= (1 << item);
    }
}

