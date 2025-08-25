package info.qbnet.jtvision.core.app;

import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TRect;

/**
 * {@code TApplication} is a simple wrapper around {@link TProgram}.
 * <p>
 * In comparison to {@link TProgram}, it only differs in its constructor. Normally,
 * you will derive your application classes from {@code TApplication}.
 * However, if you need a different sequence of subsystem initialization
 * and shutdown, you can derive your application directly from {@link TProgram}
 * and manually handle Turbo Vision subsystems together with your own.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Provides default application setup and teardown via its constructor.</li>
 *   <li>Handles standard menu commands through {@link #handleEvent(TEvent)}.</li>
 *   <li>Provides helper methods to tile and cascade windows on the desktop.</li>
 * </ul>
 *
 * <h2>Threading</h2>
 * <p>
 * Unless explicitly documented otherwise, instances are expected to be
 * created and interacted with on a single UI thread. This class is not
 * designed to be thread-safe.
 * </p>
 *
 * @see TProgram
 */
public class TApplication extends TProgram {

    /**
     * Constructs a new application configured to use the provided backend.
     * <p>
     * Typical backends include Swing, JavaFX, or others exposed via
     * {@link BackendType}. The constructor forwards the type to the
     * superclass so that the screen, input, and event loop are initialized
     * accordingly.
     * </p>
     *
     * @param type the rendering/input backend to use; must not be {@code null}
     * @throws IllegalArgumentException if {@code type} is {@code null}
     * @implNote The current implementation relies on {@link TProgram}'s
     *           initialization logic. Validation and backend-specific setup
     *           should be kept here if future backends require it.
     */
    public TApplication(BackendType type)  {
        super(type);

        logger.debug("{} TApplication@TApplication(type={})", getLogName(), type);
    }

    /**
     * Calls {@link #getTileRect(TRect)} to obtain the region over which
     * windows should cascade. If {@code desktop} is not {@code null},
     * invokes the desktop's {@code cascade} method with the computed
     * tiling rectangle.
     */
    public void cascade() {
        TRect r = new TRect();
        getTileRect(r);
        if (desktop != null) {
            desktop.cascade(r);
        }
    }

    /**
     * Sets {@code r} to the rectangle on the desktop that tiled or cascaded
     * windows should cover. By default, this method returns the extent of the
     * entire desktop view by calling {@code desktop.getExtent(r)}.
     * Both {@link #cascade()} and {@link #tile()} use this method to determine
     * the area for rearranging windows.
     * <p>
     * Your application can override getTileRect to return a different rectangle,
     * for example to exclude areas covered by message windows.
     * </p>
     *
     * @param r output parameter receiving the computed tiling/cascading bounds;
     *          must not be {@code null}
     * @throws NullPointerException if {@code r} is {@code null}
     */
    public void getTileRect(TRect r) {
        desktop.getExtent(r);
    }

    /**
     * Handles most events by first delegating to {@link TProgram#handleEvent(TEvent)},
     * then responds to standard application commands:
     * <ul>
     *   <li>{@link Command#CM_TILE} &rarr; calls {@link #tile()}.</li>
     *   <li>{@link Command#CM_CASCADE} &rarr; calls {@link #cascade()}.</li>
     * </ul>
     * <p>
     * If a command is handled, the event is cleared via {@link #clearEvent(TEvent)}
     * to signal that no further processing is required.
     * </p>
     *
     * @param event the event to handle; must not be {@code null}
     * @implSpec This override preserves the chain of responsibility by first
     *           invoking {@code super.handleEvent(event)}.
     */
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

    /**
     * Calls {@link #getTileRect(TRect)} to obtain the region over which
     * windows should tile. If {@code desktop} is not {@code null},
     * invokes the desktop's {@code tile} method with the computed
     * tiling rectangle.
     */
    public void tile() {
        TRect r = new TRect();
        getTileRect(r);
        if (desktop != null) {
            desktop.tile(r);
        }
    }

}
