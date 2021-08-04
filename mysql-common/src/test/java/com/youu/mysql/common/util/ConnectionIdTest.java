package com.youu.mysql.common.util;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

public class ConnectionIdTest {

    @Test
    public void get() throws InterruptedException {
        Assert.assertEquals(ConnectionId.get(), ConnectionId.get());
        List<Integer> ids = Lists.newArrayList();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        new Thread(() -> {
            ids.add(ConnectionId.get());
            countDownLatch.countDown();
        }).start();
        new Thread(() -> {
            ids.add(ConnectionId.get());
            countDownLatch.countDown();
        }).start();
        countDownLatch.await();
        System.out.println(ids);
        Assert.assertEquals(ConnectionId.get(), ConnectionId.get());
    }
}