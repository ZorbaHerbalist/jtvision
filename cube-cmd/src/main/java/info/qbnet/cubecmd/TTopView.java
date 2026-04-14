package info.qbnet.cubecmd;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.PaletteDescriptor;
import info.qbnet.jtvision.util.PaletteRole;
import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.TPalette;
import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TView;

public class TTopView extends THideView {
    private static final int SIDE_MARGIN = 5;

    public enum TopViewColor implements PaletteRole {
        ACTIVE_TEXT,
        PASSIVE_TEXT
    }

    public static final PaletteDescriptor<TopViewColor> TOP_VIEW_PALETTE =
            PaletteDescriptor.register("topView", TopViewColor.class);

    static final int CM_CUBE_CHANGE_DRIVE = 50_001;
    static final int CM_CUBE_CHANGE_DIR = 50_002;

    private final TFilePanelRoot panel;
    public TTopView(TRect bounds, TFilePanelRoot panel) {
        super(bounds);
        this.panel = panel;
        this.eventMask = 0xFFFF;
        this.options |= Options.OF_FIRST_CLICK;
    }

    @Override
    public void draw() {
        if (!updateDynamicBounds()) {
            return;
        }

        TDrawBuffer buffer = new TDrawBuffer();
        short color = panel.getState(State.SF_SELECTED)
                ? getColor(TopViewColor.ACTIVE_TEXT)
                : getColor(TopViewColor.PASSIVE_TEXT);

        String text = displayText();
        int offset = Math.max(0, (getSize().x - text.length()) / 2);

        buffer.moveChar(0, ' ', color, getSize().x);
        buffer.moveStr(offset, text, color);
        writeLine(0, 0, getSize().x, 1, buffer.buffer);
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_MOUSE_DOWN) {
            TPoint p = new TPoint();
            makeLocal(event.mouse.where, p);

            String text = displayText();
            int offset = Math.max(0, (getSize().x - text.length()) / 2);
            int colonPos = text.indexOf(':');
            int separator = colonPos >= 0 ? offset + colonPos : -1;

            while (mouseEvent(event, TEvent.EV_MOUSE_AUTO + TEvent.EV_MOUSE_MOVE)) {
                // wait for mouse release
            }
            clearEvent(event);

            if (separator >= 0 && p.x <= separator) {
                TView.message(panel, TEvent.EV_COMMAND, CM_CUBE_CHANGE_DRIVE, null);
            } else {
                TView.message(panel, TEvent.EV_COMMAND, CM_CUBE_CHANGE_DIR, null);
            }
        }
    }

    @Override
    public TPalette getPalette() {
        return TOP_VIEW_PALETTE.palette();
    }

    static String compactPath(String path, int width) {
        if (path == null) {
            return "";
        }
        if (path.length() <= width) {
            return path;
        }
        if (width <= 3) {
            return ".".repeat(Math.max(0, width));
        }

        int keep = width - 3;
        int left = Math.max(1, keep / 2);
        int right = Math.max(1, keep - left);
        return path.substring(0, left) + "..." + path.substring(path.length() - right);
    }

    private String displayText() {
        int availableWidth = availableCaptionWidth();
        if (availableWidth <= 2) {
            return "";
        }
        return compactPath(panel.getDirectoryName(), Math.max(1, availableWidth - 2));
    }

    private boolean updateDynamicBounds() {
        String text = displayText();
        int availableWidth = availableCaptionWidth();
        int panelLeft = panel.getOrigin().x;
        int y = panel.getOrigin().y - 1;
        if (availableWidth <= 2) {
            int x = panelLeft + SIDE_MARGIN;
            if (getOrigin().x != x || getOrigin().y != y || getSize().x != 0 || getSize().y != 1) {
                locate(new TRect(x, y, x, y + 1));
            }
            return false;
        }

        int desiredWidth = Math.min(availableWidth, text.length() + 2);

        int panelWidth = Math.max(1, panel.getSize().x);
        int x = panelLeft + SIDE_MARGIN + Math.max(0, (availableWidth - desiredWidth) / 2);

        if (getOrigin().x != x || getOrigin().y != y || getSize().x != desiredWidth || getSize().y != 1) {
            locate(new TRect(x, y, x + desiredWidth, y + 1));
        }
        return true;
    }

    private int availableCaptionWidth() {
        int panelWidth = Math.max(1, panel.getSize().x);
        int available = panelWidth - 2 * SIDE_MARGIN;
        return Math.max(1, available);
    }
}
