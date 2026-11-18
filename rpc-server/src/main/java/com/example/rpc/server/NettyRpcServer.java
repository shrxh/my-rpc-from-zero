/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.server;

import com.example.rpc.common.service.OrderService;
import com.example.rpc.common.service.UserService;
import com.example.rpc.server.service.OrderServiceImpl;
import com.example.rpc.server.service.UserServiceImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * 启动类
 */
public class NettyRpcServer {
    public static void main(String[] args) {
        // 注册服务
        ServiceRegistry.register(UserService.class.getName(), new UserServiceImpl());
        ServiceRegistry.register(OrderService.class.getName(), new OrderServiceImpl());

        // 1. 主从 Reactor 模型
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);// 老板
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();// 服务员

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 配置 ChannelPipeline（处理责任链）
                        ch.pipeline()
                            // 解码器：把字节流转成 Java 对象
                            .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
                            // 编码器：把 Java 对象转成字节流
                            .addLast(new ObjectEncoder())
                            // 自定义的业务处理器
                            .addLast(new NettyRpcServerHandler());
                    }
                });
            ChannelFuture future = bootstrap.bind(8899).sync();
            System.out.println("Netty RPC 服务端已启动，监听 8899 ...");
            future.channel().closeFuture().sync(); // 阻塞等待关闭
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
