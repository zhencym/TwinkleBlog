package com.yuming.blog.handler;

import com.alibaba.fastjson.JSON;
import com.yuming.blog.vo.Result;
import com.yuming.blog.constant.StatusConst;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 注销处理
 *
 */
@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        // 直接返回一个写着注销成功的Servlet页面
        httpServletResponse.getWriter().write(JSON.toJSONString(new Result(true, StatusConst.OK,"注销成功")));
    }

}
