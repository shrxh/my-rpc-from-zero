/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务端执行完反射调用后，把结果（或异常）封装成这个对象回传。
 * V0 还没有 requestId —— 因为一次 Socket 连接只对应一次请求-响应，不需要关联。
 * V1/V2 引入连接复用和异步后，requestId 才会登场。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    // 新增：响应带回的请求 ID
    private String requestId;
    private int code;        // 200 成功，500 失败
    private String message;  // 失败时的异常信息
    private Object data;     // 成功时的返回值

    public static RpcResponse success(Object data) {
        return RpcResponse.builder().code(200).message("OK").data(data).build();
    }

    public static RpcResponse fail(String message) {
        return RpcResponse.builder().code(500).message(message).build();
    }
}
