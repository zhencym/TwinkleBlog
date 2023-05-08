package com.yuming.blog.exception;



/**
 * 服务器异常
 *
 */
public class ServeException extends RuntimeException {
    public ServeException(String message) {
        super(message);
    }

}
