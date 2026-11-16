/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.client;

/**
 * 动态代理的核心：
 * - Proxy.newProxyInstance 生成一个实现了 UserService 接口的代理对象；
 * - 代理对象上任何方法调用，都会进入下面的 invoke()；
 * - invoke() 负责把"方法调用"翻译成"网络请求"，再把响应翻译回返回值。
 * 这就是"像调用本地方法一样调用远程方法"的本质 —— 调用方看到的接口是假的，真身在这里。
 */
public class RpcClientProxy {

}
