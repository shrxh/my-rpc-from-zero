/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.client;

import com.example.rpc.common.codec.RpcDecoder;
import com.example.rpc.common.codec.RpcEncoder;
import com.example.rpc.common.entity.RpcRequest;
import com.example.rpc.common.entity.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @since 2026/11/18
 */
public class NettyRpcClientProxy implements InvocationHandler {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8899;

    public <T> T getProxy(Class<T> interfaceClass) {
        return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, this);
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        RpcRequest rpcRequest = RpcRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .interfaceName(method.getDeclaringClass().getName())
            .methodName(method.getName())
            .parameterTypes(method.getParameterTypes())
            .parameters(args)
            .build();

        // 每次调用新建一个 Netty 客户端连接（V2 为了演示）
        NioEventLoopGroup group = new NioEventLoopGroup();
        NettyRpcClientHandler clientHandler = new NettyRpcClientHandler();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                            .addLast(new RpcDecoder())
                            .addLast(new RpcEncoder())
                            .addLast(clientHandler);
                    }
                });
            // 连接服务端
            Channel channel = bootstrap.connect(HOST, PORT).sync().channel();

            // 发送请求
            channel.writeAndFlush(rpcRequest).sync();

            // 【V2 的无奈之举】这里必须阻塞等一下，因为 Netty 是异步的。
            // 我们要等 NettyRpcClientHandler 的 channelRead0 收到数据。
            // 这里为了简单先写个轮询等待，V6 我们一定会用 CompletableFuture 干掉它。
            long startTime = System.currentTimeMillis();
            while (clientHandler.getRpcResponse() == null) {
                Thread.sleep(10);
                if(System.currentTimeMillis() - startTime > 5000) {
                    throw new RuntimeException("调用超时");
                }
            }

            RpcResponse rpcResponse = clientHandler.getRpcResponse();
            if(!rpcResponse.getRequestId().equals(rpcRequest.getRequestId())) {
                throw new RuntimeException("调用超时");
            }
            if (rpcResponse.getCode() != 200) {
                throw new RuntimeException("业务执行失败: " + rpcResponse.getMessage());
            }
            return rpcResponse.getData();
        } finally {
            group.shutdownGracefully();
        }
    }
}
