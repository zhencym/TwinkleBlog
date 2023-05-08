package com.yuming.blog.login.impl;

import com.alibaba.fastjson.JSON;
import com.yuming.blog.dto.UserInfoDTO;
import com.yuming.blog.login.SessionManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Author: zhencym
 * @DATE: 2023/5/5
 * 登录session管理
 */
@Component
public class SessionManagerImpl implements SessionManager {



  protected final Logger logger = LoggerFactory.getLogger(SessionManagerImpl.class);

  /**
   * 用户的 userInfoId 映射到的多个 sessionID
   * 因为一个用户可能有多个session
   */
  private final static ConcurrentMap<Integer, Set<String>> sessionIDs = new ConcurrentHashMap<>();
  /**
   * sessionID对应的Session
   */
  private final static ConcurrentMap<String, HttpSession> sessions = new ConcurrentHashMap<>();

  /**
   * userInfoId 对应的 userInfoDTO
   */
  private final static ConcurrentMap<String, UserInfoDTO> userInfoDTOs = new ConcurrentHashMap<>();


  /**
   * 添加
   * @param userInfoId
   * @param session
   */
  @Override
  public void addSession(Integer userInfoId, HttpSession session) {
    Set<String> set = sessionIDs.getOrDefault(userInfoId,new HashSet<>());
    set.add(session.getId());
    sessionIDs.put(userInfoId, set);
    sessions.put(session.getId(), session);
  }

  /**
   * 保存属性
   */
  @Override
  public void setAttribute(String sessionID, String key, Object value) {
    HttpSession session = sessions.get(sessionID);
    session.setAttribute(key, value);
  }

  /**
   * 删除属性
   */
  @Override
  public void removeAttribute(String sessionID, String key) {
    HttpSession session = sessions.get(sessionID);
    session.removeAttribute(key);
  }

  /**
   * 强制下线，删除用户所有session
   */
  @Override
  public void expireSession(Integer userInfoId) {
    // 找出这个用户的所有session
    Set<String> set = sessionIDs.get(userInfoId);
    // 遍历删session
    for (String sessionID : set) {
      HttpSession session = sessions.get(sessionID);
      UserInfoDTO userInfoDTO = (UserInfoDTO) session.getAttribute("userInfoDTO");
      sessions.remove(sessionID);
      //session.invalidate();
      session.removeAttribute("userInfoDTO");
      logger.info("强制下线用户 UserInfoId {} SessionID {} userNickname {} ", userInfoId, sessionID, JSON.toJSONString(userInfoDTO.getNickname()));
    }
    sessionIDs.remove(userInfoId);
  }

  /**
   * 删除一个session
   */
  @Override
  public void deleteSession(String sessionID) {
    // 找出sessionID对应的用户信息
    HttpSession session = sessions.get(sessionID);
    if(session != null) {
      UserInfoDTO userInfoDTO = (UserInfoDTO) session.getAttribute("userInfoDTO");
      // 删除sessionID,和session
      Integer userInfoId = userInfoDTO.getUserInfoId();
      sessionIDs.get(userInfoId).remove(sessionID);
      sessions.remove(sessionID);
      //session.invalidate();
      session.removeAttribute("userInfoDTO");
      // 注销也可以使用清空用户信息，但这里采用删除session的方式
      logger.info("用户注销成功 UserInfoId {} SessionID {} userNickname {} ", userInfoId, sessionID, JSON.toJSONString(userInfoDTO.getNickname()));
    }
  }

  @Override
  public List<UserInfoDTO> getUserInfoDTOList() {

    List<UserInfoDTO> list = new ArrayList<>();
    // 迭代器式遍历，保证安全
    Iterator<Entry<String, HttpSession>> iterator = sessions.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<String, HttpSession> e = iterator.next();
      UserInfoDTO userInfoDTO;
      try {
        userInfoDTO = (UserInfoDTO) e.getValue().getAttribute("userInfoDTO");
      } catch (IllegalStateException illeal) { //处理异常过期的
        logger.info("删除过期session");
        iterator.remove(); //安全删除
        continue;
      }
      list.add(userInfoDTO);
    }
      return list;
    }





}
