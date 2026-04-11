package info.qbnet.cubecmd;

import java.io.File;

public abstract class TDrive {

    private TFilePanelRoot owner;

    TDrive(TFilePanelRoot owner) {
        this.owner = owner;
    }

    abstract public TFileCollection getDirectory();
    abstract public File getCurrentDirectory();
    abstract public boolean goToParent();
    abstract public boolean enterDirectory(TFileRec rec);

}
