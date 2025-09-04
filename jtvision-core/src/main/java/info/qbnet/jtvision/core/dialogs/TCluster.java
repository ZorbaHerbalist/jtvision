package info.qbnet.jtvision.core.dialogs;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;

import java.util.ArrayList;
import java.util.List;

public abstract class TCluster extends TView {

    protected int value = 0;
    protected int sel = 0;
    protected int enableMask = 0xFFFFFFFF;
    private final List<String> strings = new ArrayList<>();

    public static final TPalette C_CLUSTER = new TPalette(TPalette.parseHexString("\\x10\\x11\\x12\\x12\\x1f"));

    TCluster(TRect bounds, List<String> strings) {
        super(bounds);
        this.options |= (Options.OF_SELECTABLE + Options.OF_FIRST_CLICK + Options.OF_PRE_PROCESS + Options.OF_POST_PROCESS);

        if (strings != null) {
            this.strings.addAll(strings);
        }

        setCursor(2, 0);
        showCursor();
    }

    @Override
    public TPalette getPalette() {
        return C_CLUSTER;
    }
}
