/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.client;

import com.example.rpc.common.entity.User;
import com.example.rpc.common.service.OrderService;
import com.example.rpc.common.service.UserService;

/**
 * V0 的验收入口：只写了"像本地调用一样"的代码，看不到任何 Socket / 序列化细节。
 * 如果这里能打印出 User(id=1, name=用户1)，说明整条链路已经打通。
 */
public class RpcClient {
    public static void main(String[] args) {
        RpcClientProxy proxy = new RpcClientProxy();

        UserService userService = proxy.getProxy(UserService.class);
        User user1 = userService.getUserById(1);
        System.out.println("getUserById(1) -> " + user1);


        OrderService orderService = proxy.getProxy(OrderService.class);
        String orderResult = orderService.createOrder("1001", "P-888");
        System.out.println("订单服务结果: " + orderResult);
    }
}
