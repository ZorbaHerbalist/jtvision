package info.qbnet.jtvdemo;

import info.qbnet.jtvision.core.app.TProgram;
import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TWindow;

import java.util.List;

public class DemoWindow extends TWindow {

    public DemoWindow(TRect bounds, String title, int count, List<String> lines) {
        super(bounds, title + ' ' + count, WN_NO_NUMBER);
        options |= Options.OF_TILEABLE;

        getClipRect(bounds);
        bounds.grow(-1, -1);
        insert(new DemoInterior(bounds, lines));
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

    private void onHideWindow() {
        System.err.println("BEGIN");
        logger.error("{} DemoWindow@onHideWindow()", getLogName());
        hide();
        System.err.println("END");
        logger.error("{} DemoWindow@onHideWindow() - DONE", getLogName());
    }
}
