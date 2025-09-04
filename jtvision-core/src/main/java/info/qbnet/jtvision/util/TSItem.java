package info.qbnet.jtvision.util;

public final class TSItem {

    public String value;
    public TSItem next;

    public TSItem(String value, TSItem next) {
        this.value = value;
        this.next = next;
    }

    public TSItem(String value) {
        this(value, null);
    }

}
