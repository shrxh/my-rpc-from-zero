/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.example.rpc.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 2026/11/17
 */
public class ServiceRegistry {
    private static Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();

    public static void register(String interfaceName, Object serviceInstance) {
        SERVICE_MAP.put(interfaceName, serviceInstance);
    }

    public static Object getService(String interfaceName) {
        return SERVICE_MAP.get(interfaceName);
    }
}
