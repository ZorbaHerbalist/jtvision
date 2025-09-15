package info.qbnet.cubecmd;

public abstract class TDrive {

    private TFilePanelRoot owner;

    TDrive(TFilePanelRoot owner) {
        this.owner = owner;
    }

    abstract public TFileCollection getDirectory();

}
