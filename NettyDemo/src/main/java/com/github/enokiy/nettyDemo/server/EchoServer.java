package com.github.enokiy.nettyDemo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {
    private static int port;

    public EchoServer(int port){
        this.port = port;
    }
    public void start() throws Exception {
        final EchoServerHandler serverHandler = new EchoServerHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try{
            bootstrap.group(group).channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port)).childHandler(serverHandler).childHandler(
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception { //当一个新的连接被接受时，一个新的子 Channel 将会被创建，而 ChannelInitializer 将会把一个你的EchoServerHandler 的实例添加到该 Channel 的 ChannelPipeline 中
                            socketChannel.pipeline().addLast(serverHandler);// pipeline 链式处理消息
                        }
                    }
            );
            ChannelFuture future = bootstrap.bind().sync();
            future.channel().closeFuture().sync();
        }catch (Exception e){

        }finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
//        if (args.length !=1){
//            System.err.println("Usage: " + EchoServer.class.getSimpleName() + " <port>");
//            return;
//        }
//        int port = Integer.parseInt(args[0]);
        int port = 45654;
        try {
            new EchoServer(port).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
