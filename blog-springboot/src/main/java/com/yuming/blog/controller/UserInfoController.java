package com.yuming.blog.controller;


import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.constant.OptLogModule;
import com.yuming.blog.constant.OptLogType;
import com.yuming.blog.constant.StatusConst;
import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.UserOnlineDTO;
import com.yuming.blog.service.UserInfoService;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.EmailVO;
import com.yuming.blog.vo.Result;
import com.yuming.blog.vo.UserInfoVO;
import com.yuming.blog.vo.UserRoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * 处理用户信息
 *
 */
@RestController
public class UserInfoController {
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 修改用户资料
     * @param userInfoVO
     * @return
     */
    @PutMapping("/users/info")
    public Result updateUserInfo(@Valid @RequestBody UserInfoVO userInfoVO) {
        userInfoService.updateUserInfo(userInfoVO);
        return new Result<>(true, StatusConst.OK, "修改成功！");
    }

    /**
     * 修改用户头像
     * @param file 用户头像
     * @return
     */
    @PostMapping("/users/avatar")
    public Result<String> updateUserInfo(MultipartFile file) {
        return new Result<>(true, StatusConst.OK, "修改成功！", userInfoService.updateUserAvatar(file));
    }

    /**
     * 绑定用户邮箱
     * @param emailVO
     * @return
     */
    @PostMapping("/users/email")
    public Result saveUserEmail(@Valid @RequestBody EmailVO emailVO) {
        userInfoService.saveUserEmail(emailVO);
        return new Result(true, StatusConst.OK, "绑定成功！");
    }

    /**
     * 修改用户角色
     * @param userRoleVO
     * @return
     */
    @OptLog(optType = OptLogType.UPDATE, optModule = OptLogModule.ROLE, optDesc = "修改用户角色")
    @PutMapping("/admin/users/role")
    public Result<String> updateUserRole(@Valid @RequestBody UserRoleVO userRoleVO) {
        userInfoService.updateUserRole(userRoleVO);
        return new Result<>(true, StatusConst.OK, "修改成功！");
    }

    /**
     * 修改用户禁用状态
     * @param userInfoId
     * @param isDisable
     * @return
     */
    @OptLog(optType = OptLogType.UPDATE, optModule = OptLogModule.ROLE, optDesc = "修改用户禁用状态")
    @PutMapping("/admin/users/disable/{userInfoId}")
    public Result updateUserSilence(@PathVariable("userInfoId") Integer userInfoId, Integer isDisable) {
        userInfoService.updateUserDisable(userInfoId, isDisable);
        return new Result<>(true, StatusConst.OK, "修改成功！");
    }

    /**
     * 查看在线用户
     * @param conditionVO
     * @return
     */
    @GetMapping("/admin/users/online")
    public Result<PageDTO<UserOnlineDTO>> listOnlineUsers(ConditionVO conditionVO) {
        return new Result<>(true, StatusConst.OK, "查询成功！", userInfoService.listOnlineUsers(conditionVO));
    }

    /**
     * 下线用户
     * @param userInfoId
     * @return
     */
    @DeleteMapping("/admin/users/online/{userInfoId}")
    public Result removeOnlineUser(@PathVariable("userInfoId") Integer userInfoId) {
        userInfoService.removeOnlineUser(userInfoId);
        return new Result<>(true, StatusConst.OK, "操作成功！");
    }

}

