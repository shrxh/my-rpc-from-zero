/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.client;

import com.example.rpc.common.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 客户端处理器 - 用于接收服务端的响应
 */
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private RpcResponse rpcResponse;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        System.out.println("客户端收到响应，requestId: " + msg.getRequestId());
        this.rpcResponse = msg;
    }

    public RpcResponse getRpcResponse() {
        return rpcResponse;
    }
}
