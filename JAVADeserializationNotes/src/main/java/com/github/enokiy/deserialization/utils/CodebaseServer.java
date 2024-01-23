package com.github.enokiy.deserialization.utils;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class CodebaseServer {
    /***
     * 启动http服务器，提供下载远程要调用的类
     *
     * @throws IOException
     */
    public static void lanuchCodebaseURLServer() throws IOException {
        System.out.println("Starting HTTP server");
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(Constants.ip, Constants.httpPort), 0);
        httpServer.createContext("/", new HttpFileHandler());
        httpServer.setExecutor(null);
        httpServer.start();
    }
}
