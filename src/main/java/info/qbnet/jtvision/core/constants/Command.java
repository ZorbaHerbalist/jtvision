package info.qbnet.jtvision.core.constants;

import java.util.HashMap;
import java.util.Map;

public class Command {

    public static final int CM_QUIT = 1;
    public static final int CM_MENU = 3;
    public static final int CM_OK = 10;
    public static final int CM_CANCEL = 11;

    public static final int CM_RELEASED_FOCUS = 51;

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
