package com.github.enokiy.nettyDemo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class EchoClient {
    private static int port;
    private static String host;

    public EchoClient(String host,int port){
        this.port = port;
        this.host = host;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        try {
            b.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress(host,port)).handler(
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel){
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new EchoClientHandler());
                        }
                    }
            );
            ChannelFuture future = b.connect().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
//            e.printStackTrace();
        }finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 45654;

        try {
            new EchoClient(host,port).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
