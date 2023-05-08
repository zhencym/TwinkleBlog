package com.yuming.blog.handler.Interceptors;

import com.yuming.blog.dto.UserInfoDTO;
import com.yuming.blog.exception.NotAccessException;
import com.yuming.blog.login.UserFilterMetadata;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Author: zhencym
 * @DATE: 2023/5/6
 * 权限拦截
 */
@Component
public class AccessInterceptor implements HandlerInterceptor {

  Logger logger = LoggerFactory.getLogger(AccessInterceptor.class);
  @Resource
  private UserFilterMetadata userFilterMetadata;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    
    logger.info("权限检查");
    // System.err.println("权限检查");
    // 放行角色
    List<String> accessRoles = userFilterMetadata.getAccessRoles(request);
    // 查询用户角色信息
    UserInfoDTO userInfoDTO = (UserInfoDTO)request.getSession().getAttribute("userInfoDTO");
    List<String> userRole = userInfoDTO.getRoleList();
    for (String role : userRole) {
      if (accessRoles.contains(role)) {
        return true;
      }
    }
    throw new NotAccessException("您没有权限！");
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView modelAndView) throws Exception {
    HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {
    HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
  }
}
