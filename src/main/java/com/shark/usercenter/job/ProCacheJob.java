package com.shark.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shark.usercenter.model.domain.User;
import com.shark.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author sharkCode
 * @date 2023/12/15 12:47
 */
@Component
@Slf4j
public class ProCacheJob {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserService userService;
    private List<Long> mainUserList = Arrays.asList(2L, 1L);
    @Resource
    private RedissonClient redissonClient;
    @Scheduled(cron = "13 2 * * * ?")
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock("shark.precachejob:docache:lock");
        try {
            // 只有一个线程可以获取到锁
            if (lock.tryLock(0, 10000L, TimeUnit.MILLISECONDS)) {
                for (Long userId: mainUserList) {
                    log.info(userId.toString());
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userList = userService.page(new Page<>(1, 10), queryWrapper);
                    try {
                        redisTemplate.opsForValue().set(String.format("brother:shark:user:%s", userId), userList);
                    } catch (Exception e) {
                        log.error("err", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

        }
    }
}
