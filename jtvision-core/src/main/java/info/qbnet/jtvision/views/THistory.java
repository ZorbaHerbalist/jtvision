package info.qbnet.jtvision.views;


import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.*;

import java.io.IOException;
import java.util.function.Consumer;

public class THistory extends TView {

    public static final int CLASS_ID = 19;

    /** Palette roles for {@link THistory}. */
    public enum HistoryColor implements PaletteRole {
        /** Arrow glyph. */
        ARROW,
        /** Sides of the history button. */
        SIDES;
    }

    public static void registerType() {
        TStream.registerType(CLASS_ID, THistory::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    private static final String HISTORY_GLYPHS = new String(new char[]{
            (char) 0xDE, '~', (char) 0x19, '~', (char) 0xDD
    });

    public static final PaletteDescriptor<HistoryColor> HISTORY_PALETTE =
            PaletteDescriptor.register("history", HistoryColor.class);

    protected TInputLine link;

    protected int historyId;

    public THistory(TRect bounds, TInputLine link, int historyId) {
        super(bounds);
        options |= Options.OF_POST_PROCESS;
        eventMask |= TEvent.EV_BROADCAST;
        this.link = link;
        this.historyId = historyId;
    }

    public THistory(TStream stream) {
        super(stream);
        try {
            TView peer = getPeerViewPtr(stream, (Consumer<TView>) v -> this.link = (TInputLine) v);
            if (peer instanceof TInputLine input) {
                link = input;
            }
            historyId = stream.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void draw() {
        TDrawBuffer buffer = new TDrawBuffer();
        buffer.moveCStr(0, HISTORY_GLYPHS, getColor(HistoryColor.SIDES, HistoryColor.ARROW));
        writeLine(0, 0, size.x, size.y, buffer.buffer);
    }

    @Override
    public TPalette getPalette() {
        return HISTORY_PALETTE.palette();
    }

    /**
     * Adds {@code text} to the history list associated with this button.
     * Empty strings are ignored to match Turbo Vision behaviour.
     */
    public void recordHistory(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        HistoryList.add(historyId, text);
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            putPeerViewPtr(stream, link);
            stream.writeInt(historyId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
