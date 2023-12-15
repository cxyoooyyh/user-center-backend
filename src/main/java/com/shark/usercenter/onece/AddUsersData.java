package com.shark.usercenter.onece;
import java.util.Date;

import com.shark.usercenter.mapper.UserMapper;
import com.shark.usercenter.model.domain.User;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * @author sharkCode
 * @date 2023/12/13 19:45
 */
//@Component
public class AddUsersData {
    @Resource
    private UserMapper userMapper;


    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void insertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < 100; i++) {
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
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
