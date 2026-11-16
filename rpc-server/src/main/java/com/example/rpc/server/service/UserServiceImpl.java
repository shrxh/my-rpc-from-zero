/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.server.service;

import com.example.rpc.common.entity.User;
import com.example.rpc.common.service.UserService;

/**
 * @since 2026/11/16
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUserById(Integer id) {
        return new User(id, "用户"+id);
    }

    @Override
    public User getUserByName(String name) {
        return new User(1, name);
    }
}
