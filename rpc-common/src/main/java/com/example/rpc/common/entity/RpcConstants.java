/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.common.entity;

/**
 * 定义协议头，固定站10个字节
 */
public class RpcConstants {
    public static final int MAGIC_NUMBER = 0xCAFEBABE; // 用于防黑客乱发数据，如果开头不是这个数，直接断开连接。
    public static final byte VERSION = 1; // 暂时写 1。为以后协议升级留后路。
    public static final byte JAVA_SERIALIZER = 1; // 暂时写 1（代表 Java 原生序列化）。V4 我们换 Kryo 时，这里写 2。
    public static final int HEAD_LENGTH = 10; // 4 + 1 + 1 + 4 = 10
}
