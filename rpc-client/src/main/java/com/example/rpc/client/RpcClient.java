/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.client;

import com.example.rpc.common.entity.User;
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

        User user2 = userService.getUserByName("用户1");
        System.out.println("getUserByName(\"用户1\") -> " + user2);
    }
}
