package info.qbnet.cubecmd;

import java.io.File;
import java.io.IOException;

public class TDiskDrive extends TDrive {

    private File drive;
    private File currentDirectory;

    public TDiskDrive(File drive, TFilePanelRoot owner, int flags)  {
        super(owner);
        this.drive = drive;
        try {
            this.currentDirectory = drive.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!currentDirectory.isDirectory()) {
            throw new RuntimeException("Not a directory: " + currentDirectory);
        }
    }

    @Override
    public TFileCollection getDirectory() {
        TFileCollection directory = new TFileCollection();
        if (currentDirectory.getParentFile() != null) {
            directory.add(TFileRec.parentEntry(currentDirectory));
        }

        File[] files = currentDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                directory.add(new TFileRec(file));
            }
        }

        return directory;
    }

    @Override
    public File getCurrentDirectory() {
        return currentDirectory;
    }

    @Override
    public boolean goToParent() {
        File parent = currentDirectory.getParentFile();
        if (parent == null || !parent.isDirectory()) {
            return false;
        }
        currentDirectory = parent;
        return true;
    }

    @Override
    public boolean enterDirectory(TFileRec rec) {
        if (rec == null || !rec.isDirectory()) {
            return false;
        }
        File target = rec.getFile();
        if (target == null || !target.isDirectory()) {
            return false;
        }
        currentDirectory = target;
        return true;
    }
}
