package com.yuming.blog.exception;

/**
 * @Author: zhencym
 * @DATE: 2023/5/7
 * 权限不足
 */
public class NotAccessException extends RuntimeException {
  public NotAccessException(String message) {
    super(message);
  }
}
