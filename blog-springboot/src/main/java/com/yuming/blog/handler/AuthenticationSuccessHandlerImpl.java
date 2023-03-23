package com.yuming.blog.handler;

import com.alibaba.fastjson.JSON;
import com.yuming.blog.dao.UserAuthDao;
import com.yuming.blog.dto.UserInfoDTO;
import com.yuming.blog.dto.UserLoginDTO;
import com.yuming.blog.entity.UserAuth;
import com.yuming.blog.utils.BeanCopyUtil;
import com.yuming.blog.utils.UserUtil;
import com.yuming.blog.vo.Result;
import com.yuming.blog.constant.StatusConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 登录成功处理
 *
 */
@Component
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {
    @Autowired
    private UserAuthDao userAuthDao;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException {
        // 更新用户ip，最近登录时间
        updateUserInfo();
        UserLoginDTO userLoginDTO = BeanCopyUtil.copyObject(UserUtil.getLoginUser(), UserLoginDTO.class);
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.getWriter().write(JSON.toJSONString(new Result<UserInfoDTO>(true, StatusConst.OK, "登录成功！", userLoginDTO)));
    }

    /**
     * 更新用户信息
     */
    @Async
    public void updateUserInfo() {
        UserAuth userAuth = UserAuth.builder()
                .id(UserUtil.getLoginUser().getId())
                .ipAddr(UserUtil.getLoginUser().getIpAddr())
                .ipSource(UserUtil.getLoginUser().getIpSource())
                .lastLoginTime(UserUtil.getLoginUser().getLastLoginTime())
                .build();
        // 主要更新登录ip、ip源、登陆时间
        userAuthDao.updateById(userAuth);
    }

}
