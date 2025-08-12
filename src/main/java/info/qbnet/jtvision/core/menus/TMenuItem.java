package info.qbnet.jtvision.core.menus;

import java.util.Objects;

public final class TMenuItem {
    public TMenuItem next;
    public String name;
    public int command;
    public boolean disabled;
    public int keyCode;
    public int helpCtx;
    public String param;
    public TMenu subMenu;

    public TMenuItem(
            TMenuItem next,
            String name,
            int command,
            boolean disabled,
            int keyCode,
            int helpCtx,
            String param,
            TMenu subMenu
    ) {
        this.next = next;
        this.name = name;
        this.command = command;
        this.disabled = disabled;
        this.keyCode = keyCode;
        this.helpCtx = helpCtx;
        this.param = param;
        this.subMenu = subMenu;
    }

    public TMenuItem next() {
        return next;
    }

    public String name() {
        return name;
    }

    public int command() {
        return command;
    }

//    public boolean disabled() {
//        return disabled;
//    }

    public int keyCode() {
        return keyCode;
    }

    public int helpCtx() {
        return helpCtx;
    }

    public String param() {
        return param;
    }

    public TMenu subMenu() {
        return subMenu;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TMenuItem) obj;
        return Objects.equals(this.next, that.next) &&
                Objects.equals(this.name, that.name) &&
                this.command == that.command &&
                this.disabled == that.disabled &&
                this.keyCode == that.keyCode &&
                this.helpCtx == that.helpCtx &&
                Objects.equals(this.param, that.param) &&
                Objects.equals(this.subMenu, that.subMenu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(next, name, command, disabled, keyCode, helpCtx, param, subMenu);
    }

    @Override
    public String toString() {
        return "TMenuItem[" +
                "next=" + next + ", " +
                "name=" + name + ", " +
                "command=" + command + ", " +
                "disabled=" + disabled + ", " +
                "keyCode=" + keyCode + ", " +
                "helpCtx=" + helpCtx + ", " +
                "param=" + param + ", " +
                "subMenu=" + subMenu + ']';
    }

}
