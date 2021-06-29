package com.youu.mysql.common.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/24
 */
public class ConnectionId {
    // Atomic integer containing the next thread ID to be assigned
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    // Thread local variable containing each thread's ID
    private static final ThreadLocal<Integer> THREAD_ID =
        ThreadLocal.withInitial(() -> NEXT_ID.getAndIncrement());

    // Returns the current thread's unique ID, assigning it if necessary
    public static int get() {
        return THREAD_ID.get();
    }
}
