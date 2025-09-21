package info.qbnet.cubecmd;

import info.qbnet.jtvision.util.PaletteDescriptor;
import info.qbnet.jtvision.util.PaletteRole;
import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.TPalette;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TScrollBar;

import java.io.File;

public class TFilePanel extends TFilePanelRoot {

    /** Palette roles used to render the file panel header row. */
    public enum FilePanelColor implements PaletteRole {
        /** Normal header text. */
        HEADER_TEXT(6),
        /** Highlighted header text (used for hotkeys). */
        HEADER_SHORTCUT(2);

        private final int index;

        FilePanelColor(int index) {
            this.index = index;
        }

        @Override
        public int index() {
            return index;
        }
    }

    public static final PaletteDescriptor<FilePanelColor> FILE_PANEL_PALETTE =
            PaletteDescriptor.register("filePanel", FilePanelColor.class);

    public TFilePanel(TRect bounds, File drive, TScrollBar scrollBar) {
        super(bounds, drive, scrollBar);
    }

    protected void drawTop(TDrawBuffer buf) {
        short color = getColor(FilePanelColor.HEADER_TEXT, FilePanelColor.HEADER_SHORTCUT);

        String format = " %-42s " + (char) 0xB3 + " %-12s " + (char) 0xB3 + " %-8s " + (char) 0xB3 + " %-7s ";
        String line = String.format(format, "Name", "Size", "Date", "Time");

        buf.moveCStr(0, line, color);
    }

    private static String formatFileName(String fileName, int width) {
        int dotIndex = fileName.lastIndexOf('.');
        String namePart = dotIndex >= 0 ? fileName.substring(0, dotIndex) : fileName;
        String extPart = dotIndex >= 0 ? fileName.substring(dotIndex + 1) : "";

        int totalLength = namePart.length() + 1 + extPart.length();

        if (totalLength <= width) {
            String formatted = String.format("%-" + (width - extPart.length()) + "s%s", namePart, extPart);
            return formatted;
        } else {
            int maxNameLength = width - extPart.length() - 2;
            if (maxNameLength < 0) maxNameLength = 0;
            String shortName = namePart.length() > maxNameLength
                    ? namePart.substring(0, maxNameLength) + ".."
                    : namePart;
            String formatted = String.format("%-" + (width - extPart.length()) + "s%s", shortName, extPart);
            return formatted;
        }
    }

    private void drawAtIdx(int idx, TDrawBuffer buf) {
        String format = " %-42s " + (char) 0xB3 + " %12s " + (char) 0xB3 + " %-8s " + (char) 0xB3 + " %-7s ";

        String line;
        if (idx >= 0 && idx < collection.size()) {
            TFileRec rec = collection.get(idx);

            line = String.format(format, formatFileName(rec.getName(), 42), rec.isDirectory() ? "<SUB-DIR>" : rec.getSize(), rec.getLastModifiedDate(), rec.getLastModifiedTime());
        } else {
            line = String.format(format, "", "", "", "");
        }

        buf.moveCStr(0, line, getColor(FilePanelColor.HEADER_TEXT, FilePanelColor.HEADER_SHORTCUT));
    }

    @Override
    public void draw() {
        // TODO
        TDrawBuffer buf = new TDrawBuffer();

        drawTop(buf);
        writeLine(0, 0, getSize().x, 1, buf.buffer);

        for (int i = 1; i < getSize().y; i++) {
            int idx = i - 1;
            drawAtIdx(idx, buf);
            writeLine(0, i, getSize().x, 1, buf.buffer);
        }

    }

    @Override
    public TPalette getPalette() {
        return FILE_PANEL_PALETTE.palette();
    }
}
