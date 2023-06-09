package com.yuming.blog.handler;

import com.alibaba.fastjson.JSON;
import com.yuming.blog.annotation.OptLog;
import com.yuming.blog.dao.OperationLogDao;
import com.yuming.blog.entity.OperationLog;
import com.yuming.blog.utils.IpUtil;
import com.yuming.blog.utils.UserUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Objects;

/**
 * 操作日志切面处理
 * 用切面实现操作记录保存
 **/
@Aspect
@Component
public class OptLogAspect {

    @Autowired
    private OperationLogDao operationLogDao;

    @Autowired
    private HttpServletRequest request;

    /**
     * 设置操作日志切入点 记录操作日志 在注解的位置切入代码
     * 即在使用了@OptLog注解的地方切入代码
     * 方便重用切入点表达式
     */
    @Pointcut("@annotation(com.yuming.blog.annotation.OptLog)")
    public void optLogPointCut() {
    }


    /**
     * 正常返回通知，拦截用户操作日志，连接点正常执行完成后执行， 如果连接点抛出异常，则不会执行
     * 后置通知，目标函数执行完后切入
     * returning，接收目标方法的返回值，同时赋值给通知方法的某个形参，
     * 在同一个切面类中使用时，直接用别名：@AfterReturning(value = "optLogPointCut()", returning = "keys")
     * 在不同切面类中使用时，输入全类名：@AfterReturning("com.yuming.handler.optLogPointCut()")
     * @param joinPoint 切入点
     * @param keys      返回结果
     *
     */
    @Transactional(rollbackFor = Exception.class)
    @AfterReturning(value = "optLogPointCut()", returning = "keys")
    public void saveOptLog(JoinPoint joinPoint, Object keys) {
        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) Objects.requireNonNull(requestAttributes).resolveReference(RequestAttributes.REFERENCE_REQUEST);

        OperationLog operationLog = new OperationLog();
        // 从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取切入点所在的方法
        Method method = signature.getMethod();
        // 获取操作
        OptLog optLog = method.getAnnotation(OptLog.class);
        // 操作模块
        operationLog.setOptModule(optLog.optModule());
        // 操作类型
        operationLog.setOptType(optLog.optType());
        // 操作描述
        operationLog.setOptDesc(optLog.optDesc());
        // 获取请求的类名
        String className = joinPoint.getTarget().getClass().getName();
        // 获取请求的方法名
        String methodName = method.getName();
        methodName = className + "." + methodName;
        // 请求方式
        operationLog.setRequestMethod(Objects.requireNonNull(request).getMethod());
        // 请求方法
        operationLog.setOptMethod(methodName);
        // 请求参数
        operationLog.setRequestParam(JSON.toJSONString(joinPoint.getArgs()));
        // 返回结果
        operationLog.setResponseData(JSON.toJSONString(keys));
        // 请求用户ID
        operationLog.setUserId(UserUtil.getLoginUser(request).getId());
        // 请求用户
        operationLog.setNickname(UserUtil.getLoginUser(request).getNickname());
        // 请求IP
        String ipAddr = IpUtil.getIpAddr(request);
        operationLog.setIpAddr(ipAddr);
        operationLog.setIpSource(IpUtil.getIpSource(ipAddr));
        // 请求URL
        operationLog.setOptUrl(request.getRequestURI());
        // 创建时间
        operationLog.setCreateTime(new Date());
        // 保存操作记录
        operationLogDao.insert(operationLog);
    }

}
