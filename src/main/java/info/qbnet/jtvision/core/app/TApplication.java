package info.qbnet.jtvision.core.app;

import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TRect;

public class TApplication extends TProgram {

    /**
     * Creates a new application using the specified backend type.
     */
    public TApplication(BackendType type)  {
        super(type);

        logger.debug("{} TApplication@TApplication(type={})", getLogName(), type);
    }

    public void cascade() {
        TRect r = new TRect();
        getTileRect(r);
        if (desktop != null) {
            desktop.cascade(r);
        }
    }

    public void getTileRect(TRect r) {
        desktop.getExtent(r);
    }

    @Override
    public void handleEvent(TEvent event) {
        boolean logEvent = LOG_EVENTS && event.what != TEvent.EV_NOTHING;
        if (logEvent) {
            logger.trace("{} TApplication@handleEvent(event={})", getLogName(), event);
        }
        super.handleEvent(event);
        if (event.what == TEvent.EV_COMMAND) {
            switch (event.msg.command) {
                case Command.CM_TILE:
                    tile();
                    break;
                case Command.CM_CASCADE:
                    cascade();
                    break;
                default:
                    return;
            }
            clearEvent(event);
        }

        if (logEvent) {
            logger.trace("{} TApplication@handleEvent() eventAfter={} handled={}",
                    getLogName(), event, event.what == TEvent.EV_NOTHING);
        }
    }

    public void tile() {
        TRect r = new TRect();
        getTileRect(r);
        if (desktop != null) {
            desktop.tile(r);
        }
    }

}
