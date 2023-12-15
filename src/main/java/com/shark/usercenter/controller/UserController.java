package com.shark.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shark.usercenter.common.BaseResponse;
import com.shark.usercenter.common.ErrorCode;
import com.shark.usercenter.common.ResultUtils;
import com.shark.usercenter.exception.BusinessException;
import com.shark.usercenter.model.domain.User;
import com.shark.usercenter.model.domain.request.UserLoginRequest;
import com.shark.usercenter.model.domain.request.UserRegisterRequest;
import com.shark.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.shark.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.shark.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * @author sharkCode
 * @date 2023/11/17 15:55
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173/"}, allowCredentials = "true")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;
    //private final static String COMMON_REDIS_USER_KEY = "brother:shark:user";
    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest httpServletRequest) {
        Object UserObj = httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) UserObj;
        if (user == null) {
            return null;
        }
        Long id = user.getId();
        // todo 校验用户是否合法
        return userService.getSafetyUser(userService.getById(id));
    }
    @PostMapping("/update")
    public BaseResponse UpdateUser(@RequestBody User updateUser, HttpServletRequest request) {
        if (updateUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.updateUser(updateUser, request));
    }
    @GetMapping("/getTagList")
    public List<User> byTagsSearchUsers(@RequestParam List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userService.searchUsersByTags(tags);
    }
    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userAccount = userRegisterRequest.getUserAccount();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
    }
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest,
    HttpServletRequest httpServletRequest) {
        if (userLoginRequest == null) {
            return null;
        }
        String userPassword = userLoginRequest.getUserPassword();
        String userAccount = userLoginRequest.getUserAccount();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        return ResultUtils.success(userService.doLogin(userAccount, userPassword, httpServletRequest));
    }
    @GetMapping("/search")
    public List<User> searchUsers(String username, HttpServletRequest httpServletRequest) {
        if (!userService.isAdmin(httpServletRequest)) {
            return new ArrayList<>();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        return userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
    }
    @GetMapping("/recommend")
    public Page<User> recommendUsers(@RequestParam("sizePage") Integer size,
                                     @RequestParam("currentPage") Integer currentPage, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ValueOperations operations = redisTemplate.opsForValue();
        String key = String.format("brother:shark:user:%s", loginUser.getId());
        Page<User> userList = (Page<User>) (operations.get(key));
        if (userList != null) return userList;
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userList = userService.page(new Page<>(currentPage, size), queryWrapper);
        operations.set(key, userList);
        return userList;
    }
    @PostMapping("/delete")
    public Boolean deleteUser(@RequestBody long id, HttpServletRequest httpServletRequest) {
         if (!userService.isAdmin(httpServletRequest)) {
            return false;
        }
        if (id <= 0) {
            return false;
        }
        return userService.removeById(id);
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public Integer userLogout(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        userService.userLogout(request);
        return 1;
    }
}
