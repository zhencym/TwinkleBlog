package com.yuming.blog.service;

import com.yuming.blog.dto.PageDTO;
import com.yuming.blog.dto.UserBackDTO;
import com.yuming.blog.dto.UserInfoDTO;
import com.yuming.blog.entity.UserAuth;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuming.blog.vo.ConditionVO;
import com.yuming.blog.vo.PasswordVO;
import com.yuming.blog.vo.UserVO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


public interface UserAuthService extends IService<UserAuth> {

    /**
     * 发送邮箱验证码
     *
     * @param username 邮箱号
     */
    void sendCode(String username);

    /**
     * 用户注册
     *
     * @param user 用户对象
     */
    void saveUser(UserVO user);

    /**
     * 注销
     * @param sessionID sessionID
     * @return
     */
    void Logout(String sessionID);

    /**
     * 普通登录
     * @param username 账号
     * @param pwd  密码
     * @return
     */
    UserInfoDTO Login(String username, String pwd, HttpServletRequest request);

    /**
     * qq登录
     *
     * @param openId      qq openId
     * @param accessToken qq token
     * @return 用户登录信息
     */
    UserInfoDTO qqLogin(String openId, String accessToken, HttpServletRequest request);


    /**
     * 修改密码
     *
     * @param user 用户对象
     */
    void updatePassword(UserVO user);

    /**
     * 修改管理员密码
     *
     * @param passwordVO 密码对象
     */
    void updateAdminPassword(PasswordVO passwordVO);

    /**
     * 查询后台用户列表
     *
     * @param condition 条件
     * @return 用户列表
     */
    PageDTO<UserBackDTO> listUserBackDTO(ConditionVO condition);

}
