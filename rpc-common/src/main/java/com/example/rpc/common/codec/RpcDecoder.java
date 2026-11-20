/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.common.codec;

import com.example.rpc.common.entity.RpcConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * 解码器
 *
 * @since 2026/11/20
 */
public class RpcDecoder extends LengthFieldBasedFrameDecoder {
    /**
     * 构造参数极其重要：
     * maxFrameLength: 最大帧长度，防止超大包攻击（比如 8M）
     * lengthFieldOffset: 长度字段的偏移量（前 6 个字节是魔数+版本+序列化算法）
     * lengthFieldLength: 长度字段占几个字节（我们写的是 int，4 个字节）
     * lengthAdjustment: 长度调整值。帧长计算公式 = lengthFieldOffset(6) + lengthFieldLength(4) + 长度字段的值 + lengthAdjustment。
     * 我们的长度字段值 fullLength 已经包含了 10 字节协议头，而上式默认又加了 offset+length 共 10 字节，
     * 多算了 10，所以填 -10 抵消，最终帧长正好等于 fullLength。
     * initialBytesToStrip: 解码后，要跳过多少个字节（把 10 字节的协议头跳过去，只把消息体交给下一个 Handler）
     */
    public RpcDecoder() {
        super(8 * 1024 * 1024,
            6,
            4,
            -10,
            RpcConstants.HEAD_LENGTH);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 父类 LengthFieldBasedFrameDecoder 已经帮我们把粘包/拆包处理好了！
        // 走到这里的 in，已经是一个完整的、剥离了 10 字节协议头的纯消息体了。
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            System.out.println("【解码器】数据不完整，继续等待... 当前可读字节: " + in.readableBytes());
            return null; // 说明当前读取的字节还不够一个完整的包，等下一次数据来
        }

        // 反序列化
        System.out.println("【解码器】成功切出一个完整的包，开始反序列化");
        byte[] bodyBytes = new byte[frame.readableBytes()];
        frame.readBytes(bodyBytes);

        ByteArrayInputStream bais = new ByteArrayInputStream(bodyBytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object obj = ois.readObject();

        frame.release(); // 释放 Netty 的 ByteBuf 内存（非常重要，防止内存泄漏）
        return obj;
    }
}
