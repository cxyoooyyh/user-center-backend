package com.shark.usercenter.service;

import com.shark.usercenter.mapper.UserMapper;
import com.shark.usercenter.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author sharkCode
 * @date 2023/12/13 20:24
 */
@SpringBootTest
public class InserUsersTest {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    private ExecutorService executorService = new ThreadPoolExecutor(60, 1000, 10000, TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(10000));

    @Test
    public void insertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        long TOTAL = 100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < TOTAL; i++) {
            User user = new User();
            user.setUsername("testUsername");
            user.setUserAccount("testAccount");
            user.setAvatarUrl("https://gw.alipayobjects.com/zos/rmsportal/OKJXDXrmkNshAMvwtvhu.png");
            user.setGender(0);
            user.setUserPassword("123123123");
            user.setPhone("13017267777");
            user.setEmail("12313@22.cc");
            user.setUserStatus(0);
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            user.setIsDelete(0);
            user.setUserRole(0);
            user.setPlanetCode("999");
            userList.add(user);
        }
        userService.saveBatch(userList, 1000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //final long TOTAL = 100000;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("testUsername");
                user.setUserAccount("testAccount");
                user.setAvatarUrl("https://gw.alipayobjects.com/zos/rmsportal/OKJXDXrmkNshAMvwtvhu.png");
                user.setGender(0);
                user.setUserPassword("123123123");
                user.setPhone("13017267777");
                user.setEmail("12313@22.cc");
                user.setUserStatus(0);
                user.setCreateTime(new Date());
                user.setUpdateTime(new Date());
                user.setIsDelete(0);
                user.setUserRole(0);
                user.setPlanetCode("999");
                userList.add(user);
                if(j % 10000 == 0) {
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName:" + Thread.currentThread().getName());
                userService.saveBatch(userList, 10000);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
