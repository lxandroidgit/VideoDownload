package com.example.sniffer.httpdownload.utils;

/**
 * Created by sniffer on 15-10-29.
 */
public class StringToInt {

    public static String toIntString(String s) {
        char c[] = s.toCharArray();
        StringBuffer number = new StringBuffer();
        for (int i = 0, length = c.length; i < length; i++) {
            if (c[i] > 47 && c[i] < 58) {
                number.append(c[i]);
            }
        }
        return number.toString();
    }
}
