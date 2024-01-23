package com.github.enokiy.kafkaDemo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Pattern DEFAULT_PATTERN = Pattern.compile("\\$\\{([^}]*?):(([^}]*?):)?([^}]*?)\\}");
        String value = "${enokiytest:..\\..\\..\\..\\..\\c:Windows:System32:drivers:etc:hosts}";
        Matcher matcher = DEFAULT_PATTERN.matcher(value);
        if (matcher.find()){
            String providerName = matcher.group(1);
            String  path = matcher.group(3) != null ? matcher.group(3) : "";
            String variable = matcher.group(4);

            System.out.println(providerName + "," + path +" ,"+ variable);
        }
    }
}
