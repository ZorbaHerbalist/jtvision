package info.qbnet.jtvdemo;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TScrollBar;
import info.qbnet.jtvision.views.TWindow;

import java.util.List;

public class DemoWindow extends TWindow {

    public DemoWindow(TRect bounds, String title, int count, List<String> lines) {
        super(bounds, title + ' ' + count, WN_NO_NUMBER);
        options |= Options.OF_TILEABLE;

        getExtent(bounds);
        TRect r = new TRect(bounds.a.x, bounds.a.y, bounds.b.x / 2 + 1, bounds.b.y);
        DemoInterior lInterior = makeInterior(r, true, lines);
        lInterior.clearGrowModes();
        lInterior.addGrowMode(GrowMode.GF_GROW_HI_Y);
        insert(lInterior);

        r = new TRect(bounds.b.x / 2, bounds.a.y, bounds.b.x, bounds.b.y);
        DemoInterior rInterior = makeInterior(r, false, lines);
        rInterior.clearGrowModes();
        rInterior.addGrowMode(GrowMode.GF_GROW_HI_X);
        rInterior.addGrowMode(GrowMode.GF_GROW_HI_Y);
        insert(rInterior);
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_COMMAND) {
            if (event.msg.command == DemoApp.CM_HIDE_WINDOW) {
                onHideWindow();
            }
        }
    }

    DemoInterior makeInterior(TRect bounds, Boolean left, List<String> lines) {
        TRect r = new TRect(bounds.b.x - 1, bounds.a.y + 1, bounds.b.x, bounds.b.y - 1);
        TScrollBar vScrollBar = new TScrollBar(r);
        vScrollBar.options |= Options.OF_POST_PROCESS;
        if (left) {
            vScrollBar.clearGrowModes();
            vScrollBar.addGrowMode(GrowMode.GF_GROW_HI_Y);
        }
        insert(vScrollBar);

        r.assign(bounds.a.x + 2, bounds.b.y - 1, bounds.b.x - 2, bounds.b.y);
        TScrollBar hScrollBar = new TScrollBar(r);
        hScrollBar.options |= Options.OF_POST_PROCESS;
        if (left) {
            hScrollBar.clearGrowModes();
            hScrollBar.addGrowMode(GrowMode.GF_GROW_HI_Y);
            hScrollBar.addGrowMode(GrowMode.GF_GROW_LO_Y);
        }
        insert(hScrollBar);

        bounds.grow(-1, -1);
        return new DemoInterior(bounds, hScrollBar, vScrollBar, lines);
    }

    private void onHideWindow() {
        System.err.println("BEGIN");
        logger.error("{} DemoWindow@onHideWindow()", getLogName());
        hide();
        System.err.println("END");
        logger.error("{} DemoWindow@onHideWindow() - DONE", getLogName());
    }
}
