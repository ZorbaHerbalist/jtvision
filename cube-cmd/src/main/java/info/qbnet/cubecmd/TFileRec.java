package info.qbnet.cubecmd;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class TFileRec {

    private final String name;
//    public String owner;
    private final long size;
//    public long packedSize;
//    public int attributes;
//    public boolean selected;
    private final long timestamp;
    private final boolean directory;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private String lastModifiedDate = null;
    private String lastModifiedTime = null;

    public TFileRec(File file) {
        this.name = file.getName();
        this.size = file.length();
        this.timestamp = file.lastModified();
        this.directory = file.isDirectory();
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public boolean isDirectory() {
        return directory;
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
