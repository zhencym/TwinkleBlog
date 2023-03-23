package com.yuming.blog.utils;

import java.time.Duration;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * Redis 分布式锁
 */
@Component
public class RedisLockUtils {

    @Autowired
    private RedisTemplate redisTemplate;

    //分布式锁过期时间 10s  可以根据业务自己调节
    private static final Long LOCK_REDIS_TIMEOUT = 10L;
    //分布式锁休眠 至 再次尝试获取 的等待时间 ms 可以根据业务自己调节
    public static final Long LOCK_REDIS_WAIT = 500L;


    /**
     *  加锁
     **/
    public Boolean getLock(String key,String value){
      Boolean lockStatus = this.redisTemplate.opsForValue().setIfAbsent(key,value, Duration.ofSeconds(LOCK_REDIS_TIMEOUT));
      return lockStatus;
    }

    /**
     *  释放锁
     **/
    public Long releaseLock(String key,String value){
      //lua脚本是原子操作，判断+删除
      String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
      RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript,Long.class);
      Long releaseStatus = (Long)this.redisTemplate.execute(redisScript, Collections.singletonList(key),value);
      return releaseStatus;
    }


}
