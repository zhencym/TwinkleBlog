package com.yuming.blog.controller;


import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.UserBackDTO;
import com.yuming.blog.dto.UserInfoDTO;
import com.yuming.blog.service.UserAuthService;
import com.yuming.blog.vo.*;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.PasswordVO;
import com.yuming.blog.vo.Result;
import com.yuming.blog.vo.UserVO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/***
 * 账号
 */
@RestController
public class UserAuthController {
    @Autowired
    private UserAuthService userAuthService;

    /**
     * 发送邮箱验证码
     * @param username 用户名
     * @return
     */
    @GetMapping("/users/code")
    public Result sendCode(String username) {
        userAuthService.sendCode(username);
        return new Result<>(true, StatusConst.OK, "发送成功！");
    }

    /**
     * 查看后台用户列表
     * @param condition
     * @return
     */
    @GetMapping("/admin/users")
    public Result<PageDTO<UserBackDTO>> listUsers(ConditionVO condition) {
        return new Result<>(true, StatusConst.OK, "查询成功！", userAuthService.listUserBackDTO(condition));
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @PostMapping("/users")
    public Result saveUser(@Valid @RequestBody UserVO user) {
        userAuthService.saveUser(user);
        return new Result<>(true, StatusConst.OK, "注册成功！");
    }

    /**
     * 修改密码
     * @param user
     * @return
     */
    @PutMapping("/users/password")
    public Result updatePassword(@Valid @RequestBody UserVO user) {
        userAuthService.updatePassword(user);
        return new Result<>(true, StatusConst.OK, "修改成功！");
    }

    /**
     * 修改管理员密码
     * @param passwordVO
     * @return
     */
    @PutMapping("/admin/users/password")
    public Result updateAdminPassword(@Valid @RequestBody PasswordVO passwordVO) {
        userAuthService.updateAdminPassword(passwordVO);
        return new Result<>(true, StatusConst.OK, "修改成功！");
    }

    /**
     * 注销
     * @return
     */
    @GetMapping ("/logout")
    public Result<UserInfoDTO> Logout(HttpServletRequest request) {
        userAuthService.Logout(request.getSession().getId());
        return new Result<>(true, StatusConst.OK, "注销成功！");
    }

    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    @PostMapping ("/login")
    public Result<UserInfoDTO> Login(String username, String password, HttpServletRequest request) {
        UserInfoDTO userInfoDTO = userAuthService.Login(username, password, request);
        if (userInfoDTO == null) {
            return new Result<>(false, StatusConst.ERROR, "账号密码错误！", null);
        }
        return new Result<>(true, StatusConst.OK, "登录成功！", userInfoDTO);
    }



    /**
     * qq登录
     * @param openId
     * @param accessToken
     * @return
     */
    @PostMapping("/users/oauth/qq")
    public Result<UserInfoDTO> qqLogin(String openId, String accessToken, HttpServletRequest request) {
        return new Result<>(true, StatusConst.OK, "登录成功！", userAuthService.qqLogin(openId, accessToken, request));
    }



}

