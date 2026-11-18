/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.server;

import com.example.rpc.common.entity.RpcRequest;
import com.example.rpc.common.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 业务处理器
 */
public class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    // 【关键点】独立的业务线程池，防止阻塞 Netty 的 EventLoop
    private static final ThreadPoolExecutor BUSINESS_EXECUTOR = new ThreadPoolExecutor(
        10, 10, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000)
    );

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        System.out.println("【服务员】接收到网络请求，当前线程：" + Thread.currentThread().getName());
        // 收到请求后，直接丢给业务线程池处理
        BUSINESS_EXECUTOR.execute(() -> {
            System.out.println("【后厨】开始执行业务，当前线程：" + Thread.currentThread().getName());
            RpcResponse rpcResponse;
            try {
                System.out.println("收到请求: " + request.getInterfaceName() + "#" + request.getMethodName());
                Object service = ServiceRegistry.getService(request.getInterfaceName());
                if (service == null) {
                    throw new RuntimeException("未找到服务: " + request.getInterfaceName());
                }
                Method method = service.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
                Object result = method.invoke(service, request.getParameters());
                rpcResponse = RpcResponse.success(result);

            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse = RpcResponse.fail(e.getMessage());
            }
            rpcResponse.setRequestId(request.getRequestId());
            // 把结果写回客户端（注意：这里是由业务线程异步触发的 writeAndFlush，Netty 内部会保证线程安全）
            ctx.writeAndFlush(rpcResponse);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
