package info.qbnet.jtvdemo;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TView;

public class DemoInterior extends TView {

    public DemoInterior(TRect bounds) {
        super(bounds);
        this.growMode |= (GrowMode.GF_GROW_HI_X + GrowMode.GF_GROW_HI_Y);
        this.options |= Options.OF_FRAMED;
    }

    @Override
    public void draw() {
        super.draw();
        writeStr(4, 2, "Hello, World!", getColor((short) 0x01));
    }
}
