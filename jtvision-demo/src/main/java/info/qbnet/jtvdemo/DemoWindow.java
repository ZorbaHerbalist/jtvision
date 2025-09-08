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
        makeInterior(bounds, lines);
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

    void makeInterior(TRect bounds, List<String> lines) {
        TScrollBar vScrollBar = standardScrollBar(ScrollBarOptions.SB_VERTICAL + ScrollBarOptions.SB_HANDLE_KEYBOARD);
        TScrollBar hScrollBar = standardScrollBar(ScrollBarOptions.SB_HORIZONTAL + ScrollBarOptions.SB_HANDLE_KEYBOARD);
        getExtent(bounds);
        bounds.grow(-1, -1);
        DemoInterior interior = new DemoInterior(bounds, hScrollBar, vScrollBar, lines);
        insert(interior);
    }

    private void onHideWindow() {
        System.err.println("BEGIN");
        logger.error("{} DemoWindow@onHideWindow()", getLogName());
        hide();
        System.err.println("END");
        logger.error("{} DemoWindow@onHideWindow() - DONE", getLogName());
    }
}
