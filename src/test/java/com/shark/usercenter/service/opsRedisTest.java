package com.shark.usercenter.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author sharkCode
 * @date 2023/12/14 20:16
 */



@SpringBootTest
public class opsRedisTest {
    @Resource
    private RedisTemplate redisTemplate;
    @Test
    public void doOpsRedis() {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set("k11", "v1");
        Object k1 = operations.get("k11");
        Assertions.assertEquals("v1", k1);
    }
}
