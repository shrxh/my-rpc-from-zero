/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.client;

import com.example.rpc.common.entity.RpcRequest;
import com.example.rpc.common.entity.RpcResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

/**
 * 动态代理的核心：
 * - Proxy.newProxyInstance 生成一个实现了 UserService 接口的代理对象；
 * - 代理对象上任何方法调用，都会进入下面的 invoke()；
 * - invoke() 负责把"方法调用"翻译成"网络请求"，再把响应翻译回返回值。
 * 这就是"像调用本地方法一样调用远程方法"的本质 —— 调用方看到的接口是假的，真身在这里。
 */
public class RpcClientProxy implements InvocationHandler {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8899;

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> interfaceClass) {
        // 注意：JDK 动态代理只能代理接口，不能代理类 —— 这是它和 CGLIB 的关键区别
        return (T) Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class<?>[]{interfaceClass},
            this
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // toString / equals / hashCode 等 Object 方法不远程调用，本地直接处理
        if(method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        // 组装请求：接口名 + 方法名 + 参数类型 + 参数值
        RpcRequest request = RpcRequest.builder()
            .interfaceName(method.getDeclaringClass().getName())
            .methodName(method.getName())
            .parameterTypes(method.getParameterTypes())
            .parameters(args)
            .build();

        // V0：每次调用新建一个 Socket，调用完关闭（简单，但连接开销大 —— 这是 V2 要换 Netty 的动机之一）
        try (Socket socket = new Socket(HOST, PORT)) {
            // 【关键顺序】客户端先建 ObjectOutputStream 并 flush 发出流头，再建 ObjectInputStream
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            oos.writeObject(request);
            oos.flush();

            RpcResponse rpcResponse = (RpcResponse) ois.readObject();
            if (rpcResponse.getCode() != 200) {
                throw new RuntimeException("调用服务失败：" + rpcResponse.getMessage());
            }
            return rpcResponse.getData();
        }
    }
}
