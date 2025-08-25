package info.qbnet.jtvision.core.constants;

import java.util.HashMap;
import java.util.Map;

public class Command {

    public static final int CM_VALID                = 0;
    public static final int CM_QUIT                 = 1;
    public static final int CM_ERROR                = 2;
    public static final int CM_MENU                 = 3;
    public static final int CM_CLOSE                = 4;
    public static final int CM_ZOOM                 = 5;
    public static final int CM_RESIZE               = 6;
    public static final int CM_NEXT                 = 7;
    public static final int CM_PREV                 = 8;
    public static final int CM_HELP                 = 9;

    public static final int CM_OK                   = 10;
    public static final int CM_CANCEL               = 11;
    public static final int CM_YES                  = 12;
    public static final int CM_NO                   = 13;
    public static final int CM_DEFAULT              = 14;

    public static final int CM_UNDO                 = 23;
    public static final int CM_TILE                 = 25;
    public static final int CM_CASCADE              = 26;

    public static final int CM_NEW                  = 30;
    public static final int CM_OPEN                 = 31;
    public static final int CM_SAVE                 = 32;
    public static final int CM_SAVE_AS              = 33;
    public static final int CM_SAVE_ALL             = 34;
    public static final int CM_CHANGE_DIR           = 35;
    public static final int CM_CLOSE_ALL            = 37;

    public static final int CM_RECEIVED_FOCUS       = 50;
    public static final int CM_RELEASED_FOCUS       = 51;
    public static final int CM_COMMAND_SET_CHANGED  = 52;

    public static final int CM_SELECT_WINDOW_NUM    = 55;

    public static final int CM_RECORD_HISTORY       = 60;

    public static final int CM_GRAB_DEFAULT         = 61;
    public static final int CM_RELEASE_DEFAULT      = 62;

//    private static final Map<Integer, Command> registry = new HashMap<>();
//
//    public static final Command CM_OK = new Command(10, "CM_OK");
//    public static final Command CM_CANCEL = new Command(11, "CM_CANCEL");
//
//    private final int code;
//    private final String name;
//
//    private Command(int code, String name) {
//        this.code = code;
//        this.name = name;
//        registry.put(code, this);
//    }
//
//    public int getCode() {
//        return code;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public static Command fromCode(int code) {
//        return registry.get(code);
//    }
//
////    public static Command fromName(String name) {
////        for (Command command : registry.values()) {
////            if (command.getName().equals(name)) {
////                return command;
////            }
////        }
////        return null;
////    }
//
//    public static Command register(int code, String name) {
//        if (registry.containsKey(code)) {
//            throw new IllegalArgumentException("Command with code " + code + " is already registered.");
//        }
//        return new Command(code, name);
//    }
//
////    static {
////        CM_OK = new Command(0, "CM_OK");
////    }
////
////    public static Command getCommand(int code) {
////        return registry.get(code);
////    }
}
