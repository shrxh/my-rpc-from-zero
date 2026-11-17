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
 * 客户端代理把"要调哪个接口、哪个方法、什么参数"封装成这个对象发给服务端。
 * 字段就是反射定位方法所需的全部信息：interfaceName + methodName + parameterTypes。
 * 少一个 parameterTypes，遇到方法重载时服务端就无法确定调哪个 Method。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {
    public static final long serialVersionUID = 1L;

    // 新增：请求唯一 ID
    private String requestId;
    private String interfaceName;      // 接口全限定名，如 com.example.rpc.common.service.UserService
    private String methodName;         // 方法名
    private Class<?>[] parameterTypes; // 参数类型数组（Class 本身实现了 Serializable）
    private Object[] parameters;       // 参数值数组（元素必须是可序列化的）
}
