package info.qbnet.jtvision.core.menus;

public record TMenuItem(
        TMenuItem next,
        String name,
        int command,
        boolean disabled,
        int keyCode,
        int helpCtx,
        String param,
        TMenu subMenu
) {
}
