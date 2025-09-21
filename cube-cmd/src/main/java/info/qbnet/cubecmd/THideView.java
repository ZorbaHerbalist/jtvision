package info.qbnet.cubecmd;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TView;

public class THideView extends TView {

    private int oldSizeX;

    public THideView(TRect bounds) {
        super(bounds);
    }

    public void hideView() {
        oldSizeX = getSize().x;
        growTo(0, getSize().y);
        eventMask = 0;
        hide();
    }

    public void showView() {
        eventMask = 0xFFFF;
        // CHECK: missing in original implementation
        growTo(oldSizeX, getSize().y);
        show();
    }

}
