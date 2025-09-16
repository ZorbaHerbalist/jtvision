package info.qbnet.jtvdemo;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.util.DataPacket;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog demonstrating a {@link FileListViewer} with scroll bars and a path input line.
 */
public class FileListDialog extends TDialog {

    private static final int CM_REFRESH = 2001;

    private final TInputLine pathInput;
    private final FileListViewer viewer;

    public FileListDialog(String title, String initialPath) {
        super(new TRect(10, 3, 80, 20), title);

        // Path input line
        pathInput = new TInputLine(new TRect(10, 2, 56, 3), 128);
        insert(pathInput);
        insert(new TLabel(new TRect(2, 2, 10, 3), "~F~older:", pathInput));

        // Scroll bars
        TScrollBar hScroll = new TScrollBar(new TRect(2, 15, 56, 16));
        insert(hScroll);
        TScrollBar vScroll = new TScrollBar(new TRect(56, 4, 57, 15));
        insert(vScroll);

        // List viewer
        viewer = new FileListViewer(new TRect(2, 4, 56, 15), 1, hScroll, vScroll, new ArrayList<>());
        insert(viewer);

        // Buttons
        insert(new TButton(new TRect(58, 14, 68, 16), "OK", Command.CM_OK, TButton.BF_DEFAULT));
        insert(new TButton(new TRect(58, 11, 68, 13), "Refresh", CM_REFRESH, 0));

        // Initial path
        DataPacket initial = new DataPacket(pathInput.dataSize())
                .putStringField(initialPath, pathInput.dataSize())
                .rewind();
        pathInput.setData(initial.getByteBuffer());
        loadDir(initialPath);
    }

    private String currentPath() {
        DataPacket packet = new DataPacket(pathInput.dataSize());
        pathInput.getData(packet.getByteBuffer());
        packet.rewind();
        return packet.getStringField(pathInput.dataSize());
    }

    private void loadDir(String dirPath) {
        viewer.setItems(buildFileList(dirPath));
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_COMMAND && event.msg.command == CM_REFRESH) {
            loadDir(currentPath());
            clearEvent(event);
        } else if (event.what == TEvent.EV_BROADCAST && event.msg.command == Command.CM_DEFAULT && current == pathInput) {
            loadDir(currentPath());
            clearEvent(event);
        }
    }

    private static List<String> buildFileList(String dirPath) {
        List<String> result = new ArrayList<>();
        Path dir = Paths.get(dirPath);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    long size = Files.size(entry);
                    FileTime ft = Files.getLastModifiedTime(entry);
                    LocalDateTime dt = LocalDateTime.ofInstant(ft.toInstant(), ZoneId.systemDefault());
                    String line = String.format("%-20s  %-10d  %s",
                            entry.getFileName().toString(),
                            size,
                            dt.format(fmt));
                    result.add(line);
                }
            }
        } catch (IOException ignored) {
        }
        return result;
    }
}

