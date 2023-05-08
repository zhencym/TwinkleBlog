package com.yuming.blog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: zhencym
 * @DATE: 2023/5/6
 */
@Getter
@AllArgsConstructor
public enum OptLogModuleEnum {

  CLASSFY(0,"分类模块"),

  BLOG_INFO(1,"博客信息模块"),

  FRIEND_LINK(2,"友链模块"),

  ARTICLE(3,"文章模块"),

  LOG(4,"日志模块"),

  TAG(5,"标签模块"),

  USER_INFO(6,"用户信息模块"),

  USER_ACCOUNT(7,"用户账号模块"),

  LEAVE_MESSAGE(8,"留言模块"),

  MENU(9,"菜单模块"),

  ROLE(10,"角色模块"),

  COMMENT(11,"评论模块"),

  RESOURCE(12,"资源模块");


  /**
   * 类型
   */
  private final int type;

  /**
   * 描述
   */
  private final String desc;

}
