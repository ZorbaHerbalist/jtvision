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

}
