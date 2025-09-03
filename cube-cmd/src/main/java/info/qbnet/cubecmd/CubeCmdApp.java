package info.qbnet.cubecmd;

import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.core.app.TApplication;

public class CubeCmdApp extends TApplication {

    public CubeCmdApp() {
        super(BackendType.JAVAFX_BITMAP);
    }

    public static void main(String[] args) {
        CubeCmdApp app = new CubeCmdApp();
        app.run();
    }
}
