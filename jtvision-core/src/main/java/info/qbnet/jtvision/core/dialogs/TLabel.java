package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.objects.TStream;
import info.qbnet.jtvision.core.views.TDrawBuffer;
import info.qbnet.jtvision.core.views.TGroup;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;

import java.io.IOException;

import static info.qbnet.jtvision.core.constants.KeyCode.getAltCode;

/**
 * Text label that can be linked to another view. When the link gains focus the
 * label is highlighted and its accelerator activates the linked view.
 */
public class TLabel extends TStaticText {

    public static final int CLASS_ID = 18;

    static {
        TStream.registerType(CLASS_ID, TLabel::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    /** View associated with this label. */
    protected TView link;

    /** Highlight flag indicating that the linked view currently has focus. */
    protected boolean light;

    public static final TPalette C_LABEL =
            new TPalette(TPalette.parseHexString("\\x07\\x08\\x09\\x09"));

    public TLabel(TRect bounds, String text, TView link) {
        super(bounds, text);
        this.link = link;
        this.options |= Options.OF_PRE_PROCESS | Options.OF_POST_PROCESS;
        this.eventMask |= TEvent.EV_BROADCAST;
    }

    public TLabel(TStream stream) {
        super(stream);
        try {
            this.link = owner != null ? owner.getSubViewPtr(stream) : null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void draw() {
        logger.trace("{} TLabel@draw()", getLogName());

        TDrawBuffer buf = new TDrawBuffer();
        short color;
        int scOff;

        if (light) {
            color = getColor((short) 0x0402);
            scOff = 0;
        } else {
            color = getColor((short) 0x0301);
            scOff = 4;
        }

        buf.moveChar(0, ' ', color & 0xFF, size.x);
        buf.moveCStr(1, getText(), color);
        if (showMarkers) {
            buf.buffer[0] = (short) ((buf.buffer[0] & 0xFF00) | SPECIAL_CHARS[scOff]);
        }
        writeLine(0, 0, size.x, 1, buf.buffer);
    }

    @Override
    public TPalette getPalette() {
        return C_LABEL;
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        switch (event.what) {
            case TEvent.EV_MOUSE_DOWN:
                focusLink(event);
                break;
            case TEvent.EV_KEYDOWN:
                char c = hotKey(getText());
                if (c != 0) {
                    if (event.key.keyCode == getAltCode(c) ||
                            (owner != null && owner.phase == TGroup.Phase.POST_PROCESS &&
                                    Character.toUpperCase(event.key.charCode) == c)) {
                        focusLink(event);
                    }
                }
                break;
            case TEvent.EV_BROADCAST:
                if (link != null &&
                        (event.msg.command == Command.CM_RECEIVED_FOCUS ||
                                event.msg.command == Command.CM_RELEASED_FOCUS)) {
                    light = (link.state & State.SF_FOCUSED) != 0;
                    drawView();
                }
                break;
            default:
                break;
        }
    }

    private void focusLink(TEvent event) {
        if (link != null && (link.options & Options.OF_SELECTABLE) != 0) {
            link.focus();
        }
        clearEvent(event);
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            if (owner != null) {
                owner.putSubViewPtr(stream, link);
            } else {
                stream.writeInt(0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

