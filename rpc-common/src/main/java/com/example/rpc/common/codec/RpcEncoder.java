/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.common.codec;

import com.example.rpc.common.entity.RpcConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * 编码器,负责把 Java 对象（RpcRequest 或 RpcResponse）变成字节流发出去
 */
public class RpcEncoder extends MessageToByteEncoder<Object>{
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 1. 先把对象序列化成 byte[]（这里暂时用 JDK 原生序列化）
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(msg);
        oos.flush();
        byte[] bodyBytes = baos.toByteArray();

        // 2. 开始写协议头
        out.writeInt(RpcConstants.MAGIC_NUMBER);
        out.writeByte(RpcConstants.VERSION);
        out.writeByte(RpcConstants.JAVA_SERIALIZER);

        // 3. 计算总长度并写入（总长度 = 协议头长度 10 + 消息体长度）
        int fullLength = RpcConstants.HEAD_LENGTH + bodyBytes.length;
        out.writeInt(fullLength);

        // 4. 把消息体写进去
        out.writeBytes(bodyBytes);
    }
}
