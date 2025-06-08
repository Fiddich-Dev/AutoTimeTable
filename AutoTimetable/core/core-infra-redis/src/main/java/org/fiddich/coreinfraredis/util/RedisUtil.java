package org.fiddich.coreinfraredis.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveAsValue(String key, Object value, Long time, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, time, timeUnit);
    }

    public String getValue(String key) {
        return String.valueOf(redisTemplate.opsForValue().get(key));
    }

    public List<Object> findAllValues(String key, int start, int end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public void deleteOneValue(String key, Object value) {
        redisTemplate.opsForList().remove(key, 1, value);
    }

    public void addOneValue(String key, String newValue) {
        redisTemplate.opsForList().rightPush(key, newValue);
    }

    public void updateExpirationTime(String key, Long time, TimeUnit timeUnit) {
        redisTemplate.expire(key, time, timeUnit);
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }




}
