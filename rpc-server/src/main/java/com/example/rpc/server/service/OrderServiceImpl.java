/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.server.service;

import com.example.rpc.common.service.OrderService;

/**
 * @since 2026/11/17
 */
public class OrderServiceImpl implements OrderService {
    @Override
    public String createOrder(String userId, String productId) {
        return "订单创建成功：用户 " + userId + " 购买了 " + productId;
    }
}
