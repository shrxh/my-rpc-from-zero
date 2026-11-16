/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.common.service;

import com.example.rpc.common.entity.User;

/**
 * 客户端和服务端共享这个接口：客户端用它生成代理，服务端用它定位实现类。
 * V0 只放两个方法，其中一个用于验证"同一套代理能支持多个方法"。
 */
public interface UserService {
    User getUserById(Integer id);
    User getUserByName(String name);
}
