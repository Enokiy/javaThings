package com.github.enokiy.deserialization.utils;

public class Constants {
    public static String ip = "127.0.0.1";
    public static int rmiPort = 6666;
    public static int ldapPort = 7777;
    public static int httpPort = 8888;
    public static String httpUrl = "http://" + ip + ":" + httpPort + "/";
    public static String cmd = "calc";
    public static String rmiUrl = "rmi://" + ip + ":" + rmiPort + "/";
    public static String ldapUrl = "ldap://" + ip + ":" + ldapPort + "/";
}
