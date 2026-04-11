package info.qbnet.cubecmd;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class TFileRec {

    private final File file;
    private final String name;
//    public String owner;
    private final long size;
//    public long packedSize;
//    public int attributes;
//    public boolean selected;
    private final long timestamp;
    private final boolean directory;
    private final String extension;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private String lastModifiedDate = null;
    private String lastModifiedTime = null;

    public TFileRec(File file) {
        this(file, file.getName(), file.length(), file.lastModified(), file.isDirectory());
    }

    private TFileRec(File file, String name, long size, long timestamp, boolean directory) {
        this.file = file;
        this.name = name;
        this.size = size;
        this.timestamp = timestamp;
        this.directory = directory;
        int dotIndex = this.name.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex + 1 < this.name.length()) {
            this.extension = this.name.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        } else {
            this.extension = "";
        }
    }

    public static TFileRec parentEntry(File currentDirectory) {
        File parent = currentDirectory.getParentFile();
        if (parent == null) {
            parent = currentDirectory;
        }
        return new TFileRec(parent, "..", 0, currentDirectory.lastModified(), true);
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public long getSize() {
        return size;
    }

    public boolean isDirectory() {
        return directory;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getExtension() {
        return extension;
    }

    public String getLastModifiedDate() {
        if (lastModifiedDate == null) {
            lastModifiedDate = dateFormat.format(new Date(timestamp));
        }
        return lastModifiedDate;
    }

    public String getLastModifiedTime() {
        if (lastModifiedTime == null) {
            lastModifiedTime = timeFormat.format(new Date(timestamp));
        }
        return lastModifiedTime;
    }

}
