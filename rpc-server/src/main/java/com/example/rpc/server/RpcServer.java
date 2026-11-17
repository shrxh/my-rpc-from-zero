/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.server;

import com.example.rpc.common.entity.RpcRequest;
import com.example.rpc.common.entity.RpcResponse;
import com.example.rpc.common.service.OrderService;
import com.example.rpc.common.service.UserService;
import com.example.rpc.server.service.OrderServiceImpl;
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
    public static void main(String[] args) throws Exception {
        // 1. 启动时，把所有的服务实现类注册到注册表里
        ServiceRegistry.register(UserService.class.getName(), new UserServiceImpl());
        ServiceRegistry.register(OrderService.class.getName(), new OrderServiceImpl());

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
                System.out.println("收到请求: " + request.getRequestId() + " -> " +
                    request.getInterfaceName() + "#" + request.getMethodName());

                RpcResponse rpcResponse = null;
                try {
                    // 2. 根据请求的接口名，从注册表里拿实现类
                    Object service = ServiceRegistry.getService(request.getInterfaceName());
                    if (service == null) {
                        throw new RuntimeException("未找到服务: " + request.getInterfaceName());
                    }

                    // 3. 反射调用
                    Method method =
                        service.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
                    Object result = method.invoke(service, request.getParameters());
                    rpcResponse = RpcResponse.success(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    rpcResponse = RpcResponse.fail(e.getMessage());
                }

                // 4. 把 requestId 原样塞回响应里
                rpcResponse.setRequestId(request.getRequestId());
                oos.writeObject(rpcResponse);
                oos.flush();
            }
        }
    }
}
