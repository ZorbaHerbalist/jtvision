package info.qbnet.jtvdemo;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TListViewer;
import info.qbnet.jtvision.views.TScrollBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple {@link TListViewer} showing a list of formatted file metadata lines.
 */
public class FileListViewer extends TListViewer {

    private List<String> items;

    public FileListViewer(TRect bounds, int numCols, TScrollBar hScroll, TScrollBar vScroll, List<String> items) {
        super(bounds, numCols, hScroll, vScroll);
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        setRange(this.items.size());
    }

    @Override
    public String getText(int item, int maxLen) {
        if (item >= 0 && item < items.size()) {
            String text = items.get(item);
            if (text.length() > maxLen) {
                return text.substring(0, maxLen);
            }
            return text;
        }
        return "";
    }

    /**
     * Replaces the displayed items and updates the viewer's range.
     */
    public void setItems(List<String> newItems) {
        this.items = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        setRange(this.items.size());
        focusItem(0);
        drawView();
    }
}

