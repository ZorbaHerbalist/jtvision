package info.qbnet.jtvision.core.app;

import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.constants.KeyCode;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.menus.TStatusItem;

public class TApplication extends TProgram {

    /**
     * Creates a new application using the specified backend type.
     */
    public TApplication(BackendType type)  {
        super(type);

        logger.debug("{} TApplication@TApplication(type={})", getLogName(), type);
    }

    @Override
    public void handleEvent(TEvent event) {
        boolean logEvent = LOG_EVENTS && event.what != TEvent.EV_NOTHING;
        if (logEvent) {
            logger.trace("{} TApplication@handleEvent(event={})", getLogName(), event);
        }
        super.handleEvent(event);

        // TODO EV_COMMAND
        if (logEvent) {
            logger.trace("{} TApplication@handleEvent() eventAfter={} handled={}",
                    getLogName(), event, event.what == TEvent.EV_NOTHING);
        }
    }

    public static TStatusItem stdStatusKeys(TStatusItem next) {
        return new TStatusItem(null, KeyCode.KB_ALT_X, Command.CM_QUIT,
               new TStatusItem(null, KeyCode.KB_F10, Command.CM_MENU,
               new TStatusItem(null, KeyCode.KB_ALT_F3, Command.CM_CLOSE,
               new TStatusItem(null, KeyCode.KB_F5, Command.CM_ZOOM,
               new TStatusItem(null, KeyCode.KB_CTRL_F5, Command.CM_RESIZE,
               new TStatusItem(null, KeyCode.KB_F6, Command.CM_NEXT,
               new TStatusItem(null, KeyCode.KB_SHIFT_F6, Command.CM_PREV,
               null)))))));
    }
}
