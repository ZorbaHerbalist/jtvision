package info.qbnet.jtvision.util;

public class CString {

    public static int cStrLen(String str) {
        int len = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '~') {
                len++;
            }
        }
        return len;
    }

}
