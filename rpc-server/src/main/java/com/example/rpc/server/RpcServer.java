/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.server;

import com.example.rpc.common.entity.RpcRequest;
import com.example.rpc.common.entity.RpcResponse;
import com.example.rpc.common.service.UserService;
import com.example.rpc.server.service.UserServiceImpl;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * V0 服务端：BIO（阻塞 IO），accept 一个连接 → 读一个 RpcRequest → 反射执行 → 写回 RpcResponse。
 * 这里"硬编码"了 UserServiceImpl，是为了让你先看清反射调用的最小形态。
 * V1 会把它换成一个 Map<String, Object> 服务注册表，从而支持任意接口。
 */
public class RpcServer {
    private static final UserService userService = new UserServiceImpl();

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8899);
        System.out.println("RPC 服务端已启动，监听 8899 ...");

        while (true) {
            // 阻塞等待客户端连接；V0 一次处理一个连接，处理完再 accept 下一个
            Socket socket = serverSocket.accept();
            System.out.println("收到连接：" + socket.getRemoteSocketAddress());

            try (socket) {
                // 【关键顺序】先建 ObjectInputStream 读客户端的流头，再建 ObjectOutputStream 写自己的流头
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                // 必须 flush：ObjectOutputStream 构造时只把流头写进缓冲区，不 flush 对方会一直阻塞在读流头上
                oos.flush();

                RpcRequest request = (RpcRequest) ois.readObject();
                System.out.println("收到请求：" + request);

                RpcResponse rpcResponse = null;
                try {
                    // 反射：根据 methodName + parameterTypes 精确定位方法（重载方法靠 parameterTypes 区分）
                    Method method =
                        UserServiceImpl.class.getMethod(request.getMethodName(), request.getParameterTypes());
                    // invoke：把 userServiceImpl 实例和参数传进去，得到返回值
                    Object result = method.invoke(userService, request.getParameters());
                    rpcResponse = RpcResponse.success(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    rpcResponse = RpcResponse.fail(e.getMessage());
                }
                oos.writeObject(rpcResponse);
                oos.flush();
            }
        }
    }
}
