/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mirth.connect.connectors.vm.VMMessageReceiver;

public class VMRegistry {
    private Map<String, VMMessageReceiver> vmRegistry = null;
    private static VMRegistry instance = null;

    private VMRegistry() {

    }

    public static VMRegistry getInstance() {
        synchronized (VMRegistry.class) {
            if (instance == null) {
                instance = new VMRegistry();
                instance.initialize();
            }

            return instance;
        }
    }

    private void initialize() {
        vmRegistry = new ConcurrentHashMap<String, VMMessageReceiver>();
    }

    public synchronized void unregister(String key) {
        vmRegistry.remove(key);
    }

    public VMMessageReceiver get(String key) {
        return vmRegistry.get(key);
    }

    public synchronized void register(String key, VMMessageReceiver value) {
        vmRegistry.put(key, value);
    }
}
