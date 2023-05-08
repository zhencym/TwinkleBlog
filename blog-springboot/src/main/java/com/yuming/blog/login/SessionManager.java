package com.yuming.blog.login;

import com.yuming.blog.dto.UserInfoDTO;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.http.HttpSession;

/**
 * @Author: zhencym
 * @DATE: 2023/5/5
 */
public interface SessionManager {
  /**
   * 添加
   * @param userInfoId
   * @param session
   */
  public void addSession(Integer userInfoId, HttpSession session);
  /**
   * 保存属性
   */
  public void setAttribute(String sessionID, String key, Object value);
  /**
   * 删除属性
   */
  public void removeAttribute(String sessionID, String key);
  /**
   * 强制下线，删除用户所有session
   */
  public void expireSession(Integer userInfoId);

  /**
   * 删除
   * @param sessionID
   */
  public void deleteSession(String sessionID);

  /**
   * 返回所有session中的用户信息
   */
  List<UserInfoDTO> getUserInfoDTOList();

}
