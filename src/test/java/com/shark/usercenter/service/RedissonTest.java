package com.shark.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shark.usercenter.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author sharkCode
 * @date 2023/12/15 18:52
 */
@SpringBootTest
@Slf4j
public class RedissonTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        RLock lock = redissonClient.getLock("shark.precachejob:docache:lock");
        try {
            // 只有一个线程可以获取到锁
            if (lock.tryLock(0, 100000L, TimeUnit.MILLISECONDS)) {
                log.info("当前取到锁的线程：" + Thread.currentThread().getName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

        }
    }
}
