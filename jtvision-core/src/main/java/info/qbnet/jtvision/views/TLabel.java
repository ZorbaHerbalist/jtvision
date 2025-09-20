package info.qbnet.jtvision.views;

import com.fasterxml.jackson.databind.node.ObjectNode;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.*;

import java.io.IOException;
import java.util.function.Consumer;

import static info.qbnet.jtvision.util.KeyCode.getAltCode;

/**
 * Text label that can be linked to another view. When the link gains focus the
 * label is highlighted and its accelerator activates the linked view.
 */
public class TLabel extends TStaticText {

    public static final int CLASS_ID = 18;

    /**
     * Palette roles for {@link TLabel}.
     */
    public enum LabelColor implements PaletteRole {
        /** Normal text. */
        NORMAL_TEXT,
        /** Highlighted text. */
        SELECTED_TEXT,
        /** Normal shortcut. */
        NORMAL_SHORTCUT,
        /** Shortcut while highlighted. */
        SELECTED_SHORTCUT;
    }

    public static void registerType() {
        TStream.registerType(CLASS_ID, TLabel::new);
        JsonViewStore.registerType(TLabel.class, TLabel::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    /** View associated with this label. */
    protected TView link;

    /** Highlight flag indicating that the linked view currently has focus. */
    protected boolean light;

    public static final PaletteDescriptor<LabelColor> LABEL_PALETTE =
            PaletteDescriptor.register("label", LabelColor.class);

    public TLabel(TRect bounds, String text, TView link) {
        super(bounds, text);
        this.link = link;
        this.options |= Options.OF_PRE_PROCESS | Options.OF_POST_PROCESS;
        this.eventMask |= TEvent.EV_BROADCAST;
    }

    public TLabel(TStream stream) {
        super(stream);
        try {
            this.link = (TView) getPeerViewPtr(stream, (Consumer<TView>) v -> this.link = v);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TLabel(ObjectNode node) {
        super(node);
        int linkIndex = JsonUtil.getInt(node, "link", 0);
        if (linkIndex > 0) {
            TView target = getPeerViewPtr(linkIndex, (Consumer<TView>) v -> this.link = v);
            if (target != null) {
                this.link = target;
            }
        }
    }

    @Override
    public void draw() {
        logger.trace("{} TLabel@draw()", getLogName());

        TDrawBuffer buf = new TDrawBuffer();
        short color;
        int scOff;

        if (light) {
            color = getColor(LabelColor.SELECTED_TEXT, LabelColor.SELECTED_SHORTCUT);
            scOff = 0;
        } else {
            color = getColor(LabelColor.NORMAL_TEXT, LabelColor.NORMAL_SHORTCUT);
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
        return LABEL_PALETTE.palette();
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
                            (getOwner() != null && getOwner().phase == TGroup.Phase.POST_PROCESS &&
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
            putPeerViewPtr(stream, link);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void storeJson(ObjectNode node) {
        super.storeJson(node);
        node.put("link", getPeerViewIndex(link));
    }

}

