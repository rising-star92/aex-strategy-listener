package com.walmart.aex.strategy.listener.utils;

public class CommonUtils {
    public static String removeDoubleQuotes(String message) {
        String result = "";
        if(message!=null && !message.trim().isEmpty() && message.length()>1) {
            char[] msg = message.toCharArray();
            Integer startIndex = 0;
            Integer endIndex = message.length();
            if (msg[0] == '\"' && msg[1] == '{') startIndex = 1;
            if (msg[message.length() - 2] == '}' && msg[message.length() - 1] == '\"') endIndex = message.length() - 1;
            result =  message.substring(startIndex, endIndex);
        }
        return result;
    }
}
