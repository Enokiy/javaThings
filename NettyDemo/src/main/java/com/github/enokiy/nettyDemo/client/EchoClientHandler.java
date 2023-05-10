package com.github.enokiy.nettyDemo.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class EchoClientHandler extends SimpleChannelInboundHandler {
    @Override
    //当从服务器接收到一条消息时被调用
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        ByteBuf msg1 = (ByteBuf)msg;
        System.out.println("client received: " + msg1.toString(CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    @Override
    //—到服务器的连接建立之后将被调用；
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));// 成功建立连接之后，发送一条消息
    }
}
