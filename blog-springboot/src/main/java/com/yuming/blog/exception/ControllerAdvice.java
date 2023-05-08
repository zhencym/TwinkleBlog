package com.yuming.blog.handler;

import com.yuming.blog.exception.ServeException;
import com.yuming.blog.vo.Result;
import com.yuming.blog.constant.StatusConst;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 全局异常处理
 * 有异常不能只在后端抛出错误，还要返回前端错误信息，告诉前端发生了什么错误。
 *
 */
@RestControllerAdvice
public class ControllerAdvice {

    /**
     * 处理服务异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = ServeException.class)
    public Result errorHandler(ServeException e) {
        return new Result(false, StatusConst.ERROR, e.getMessage());
    }

    /**
     * 处理参数异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result errorHandler(MethodArgumentNotValidException e) {
        return new Result(false, StatusConst.ERROR, e.getBindingResult().getFieldError().getDefaultMessage());
    }

    /**
     * 处理系统异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public Result errorHandler(Exception e) {
        e.printStackTrace();
        return new Result(false, StatusConst.SYSTEM_ERROR, "系统异常");
    }

}
