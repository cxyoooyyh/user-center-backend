package com.shark.usercenter.service;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import com.shark.usercenter.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

@SpringBootTest
class UserServiceTest {
    @Resource
    private UserService userService;
    @Test
    void testDigest() throws NoSuchAlgorithmException {
        String s = DigestUtils.md5DigestAsHex(("123" + "mypassword").getBytes());
        System.out.println(s);
    }
    @Test
    void testAddUser() {
//        User user = new User();
//        user.setUsername("testShark");
//        user.setUserAccount("12345");
//        user.setAvatarUrl("https://pic.code-nav.cn/user_avatar/1666714005462339585/luu9j6X1-%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20230626160847.jpg");
//        user.setGender(0);
//        user.setUserPassword("xxx");
//        user.setPhone("123");
//        user.setEmail("456");
//        boolean result = userService.save(user);
//        System.out.println(user.getId());
//        Assertions.assertTrue(result);
    }

    @Test
    void userRegister() {

    }


    @Test
    public void searchUsersByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        Assert.assertNotNull(userList);
    }
}