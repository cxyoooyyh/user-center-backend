package com.shark.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shark.usercenter.common.ErrorCode;
import com.shark.usercenter.exception.BusinessException;
import com.shark.usercenter.model.domain.User;
import com.shark.usercenter.service.UserService;
import com.shark.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.shark.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.shark.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
* @author admin_oyh
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2023-11-11 22:36:42
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    /**
     * 密码加密 盐
     */
    private static final String SALT = "sharkCode";
    /**
     * 用户登录态 键
     */

    @Resource
    private UserMapper userMapper;
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            // todo 封装异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不合法");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不合法");
        }
        if (planetCode.length() > 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不合法");
        }
        // 校验账户不包含特殊字符
        String regEx = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不合法");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不相同");
        }
        // 账户不能重复，查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "数据重复");
        }
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "数据重复");
        }

        // 密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        System.out.println(encryptPassword);
        // 添加操作
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return user.getId();
    }

    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 校验账户不包含特殊字符
        String regEx = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, user Account match userPassword");
            throw new BusinessException(ErrorCode.NOT_LOGIN, "登錄失敗！");
        }
        // 用户脱敏
        User safetyUser = this.getSafetyUser(user);
        // 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 请求用户注销
     * @param httpServletRequest
     * @return
     */
    @Override
    public void userLogout(HttpServletRequest httpServletRequest) {
        httpServletRequest.getSession().removeAttribute(USER_LOGIN_STATE);
    }

    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName: tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 更新用户
     * @param updateUser
     * @param request
     * @return
     */
    @Override
    public Integer updateUser(User updateUser, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        Long id = updateUser.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (!isAdmin(request) && !id.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(id);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userMapper.updateById(updateUser);
    }

    /**
     * 用户是否登录
     * @param request
     * @return
     */
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) throw new BusinessException(ErrorCode.NO_AUTH);
        return (User) userObj;
    }
    @Override
    public boolean isAdmin(HttpServletRequest httpServletRequest)  {
        Object userObj = httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null || user.getUserRole() != ADMIN_ROLE) {
            return false;
        }
        return true;
    }
}




