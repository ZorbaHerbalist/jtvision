package info.qbnet.jtvision.util;

import java.util.Objects;

public final class TMenu {
    public  TMenuItem items;
    public  TMenuItem defaultItem;

    public TMenu(TMenuItem items, TMenuItem defaultItem) {
        this.items = items;
        this.defaultItem = defaultItem;
    }

    public TMenuItem items() {
        return items;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TMenu) obj;
        return Objects.equals(this.items, that.items) &&
                Objects.equals(this.defaultItem, that.defaultItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, defaultItem);
    }

    @Override
    public String toString() {
        return "TMenu[" +
                "items=" + items + ", " +
                "defaultItem=" + defaultItem + ']';
    }

}
