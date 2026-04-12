package info.qbnet.cubecmd;

import info.qbnet.jtvision.util.PaletteDescriptor;
import info.qbnet.jtvision.util.PaletteRole;
import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.TPalette;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TScrollBar;

import java.io.File;

public class TFilePanel extends TFilePanelRoot {

    /** Palette roles mirroring Dos Navigator's CPanel palette layout. */
    public enum FilePanelColor implements PaletteRole {
        /** Standard file list text. */
        NORMAL_TEXT,
        /** Column separators and highlighted markers. */
        SEPARATOR,
        /** Selected row when the panel is inactive. */
        SELECTED_TEXT,
        /** Cursor row base color while focused (normal file). */
        CURSOR_NORMAL,
        /** Cursor row color while focused on a selected file. */
        CURSOR_SELECTED,
        /** Header/column titles text. */
        HEADER_TEXT,
        /** Custom highlight color 1 (DN ttCust1). */
        CUSTOM_TYPE_1,
        /** Custom highlight color 2 (DN ttCust2). */
        CUSTOM_TYPE_2,
        /** Custom highlight color 3 (DN ttCust3). */
        CUSTOM_TYPE_3,
        /** Custom highlight color 4 (DN ttCust4). */
        CUSTOM_TYPE_4,
        /** Custom highlight color 5 (DN ttCust5). */
        CUSTOM_TYPE_5,
        /** Reserved DN palette slot (index 12). */
        RESERVED_12,
        /** Reserved DN palette slot (index 13). */
        RESERVED_13,
        /** Reserved DN palette slot (index 14). */
        RESERVED_14
    }

    public static final PaletteDescriptor<FilePanelColor> FILE_PANEL_PALETTE =
            PaletteDescriptor.register("filePanel", FilePanelColor.class);

    public TFilePanel(TRect bounds, File drive, TScrollBar scrollBar) {
        super(bounds, drive, scrollBar);
    }

    protected void drawTop(TDrawBuffer buf) {
        short color = getColor(FilePanelColor.HEADER_TEXT, FilePanelColor.NORMAL_TEXT);

        String separator = "~" + (char) 0xB3 + "~";
        String format = " %-42s " + separator + " %-12s " + separator + " %-8s " + separator + " %-7s ";
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
        String separator = "~" + (char) 0xB3 + "~";
        String format = " %-42s " + separator + " %12s " + separator + " %-8s " + separator + " %-7s ";

        String line;
        boolean currentRow = idx == collection.getSelected();
        boolean focused = (state & State.SF_FOCUSED) != 0;
        FilePanelColor textRole = currentRow && focused
                ? FilePanelColor.CURSOR_NORMAL
                : FilePanelColor.NORMAL_TEXT;
        int normalAttr = getColor(textRole) & 0xFF;
        int separatorAttr = getColor(FilePanelColor.NORMAL_TEXT) & 0xFF;
        if (currentRow && focused) {
            separatorAttr = (normalAttr & 0xF0) | (separatorAttr & 0x0F);
        }
        short color = (short) ((separatorAttr << 8) | normalAttr);

        if (idx >= 0 && idx < collection.visibleSize()) {
            TFileRec rec = collection.visibleGet(idx);

            line = String.format(format, formatFileName(rec.getName(), 42), rec.isDirectory() ? "<SUB-DIR>" : rec.getSize(), rec.getLastModifiedDate(), rec.getLastModifiedTime());
        } else {
            line = String.format(format, "", "", "", "");
        }

        buf.moveCStr(0, line, color);
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

        int selectedRow = collection.getSelected() + 1;
        if ((state & State.SF_FOCUSED) != 0 && selectedRow > 0 && selectedRow < getSize().y) {
            setCursor(1, selectedRow);
            showCursor();
        } else {
            hideCursor();
        }
    }

    @Override
    public TPalette getPalette() {
        return FILE_PANEL_PALETTE.palette();
    }
}
