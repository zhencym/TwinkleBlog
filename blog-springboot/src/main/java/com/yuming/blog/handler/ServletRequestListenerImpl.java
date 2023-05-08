package com.yuming.blog.handler;


import com.yuming.blog.utils.IpUtil;
import com.yuming.blog.utils.RedisLockUtils;
import com.yuming.blog.constant.RedisPrefixConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * request监听
 * 对每个请求预处理，统计ip，以便增加访问量，用户量
 *
 */
@Component
public class ServletRequestListenerImpl implements ServletRequestListener {

    Logger logger = LoggerFactory.getLogger(ServletRequestListenerImpl.class);
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisLockUtils redisLockUtils;

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        //获取上一次访问的session和ip地址！！注意此时是访问预处理时期，session
        HttpSession session = request.getSession();
        // 获取判断当前ip是否访问，增加访问量

        // 这里加锁性能会下降很多，因为所有请求都经过这里。所以就先不加锁了。
        String ipAddr = IpUtil.getIpAddr(request);
        String ip = (String) session.getAttribute("ip");
            if (!ipAddr.equals(ip)) {
                //上次访问session中的ip根本次访问request中的ip不一样，就更新session中的ip
                session.setAttribute("ip", ipAddr);
                redisTemplate.boundValueOps(RedisPrefixConst.BLOG_VIEWS_COUNT).increment(1);
                logger.info("增加访问量，新IP {}", ipAddr);
            }
            // 将ip存入redis，统计每日用户量
            redisTemplate.boundSetOps(RedisPrefixConst.IP_SET).add(ipAddr);
    }

    @Scheduled(cron = " 0 1 0 * * ?")//每天0点10分执行一次
    private void clear() {
        //清空redis中的ip
        redisTemplate.delete(RedisPrefixConst.IP_SET);
    }


}
