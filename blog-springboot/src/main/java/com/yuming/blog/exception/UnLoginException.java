package com.yuming.blog.exception;

/**
 * @Author: zhencym
 * @DATE: 2023/5/6
 * 未登录异常
 */
public class UnLoginException extends RuntimeException {
  public UnLoginException(String message) {
    super(message);
  }

}