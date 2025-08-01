package info.qbnet.jtvision.core.event;

public class CommandEvent implements Event {

    private final int command;
    private final int info;

    public CommandEvent(int command, int info) {
        this.command = command;
        this.info = info;
    }

    public int getCommand() {
        return command;
    }

    public int getInfo() {
        return info;
    }

}
