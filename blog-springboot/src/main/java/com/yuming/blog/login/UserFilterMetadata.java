package com.yuming.blog.login;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: zhencym
 * @DATE: 2023/5/5
 */
public interface UserFilterMetadata {

  /**
   * 清空接口角色信息
   */
  public void clearDataSource();
  /**
   * 返回了List<String>，表示当前请求对应资源权限 可以访问的 角色列表
   * @param request
   * @return
   * @throws IllegalArgumentException
   */
  public List<String> getAccessRoles(HttpServletRequest request);

}
