package com.henry.common.redis.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisUtil {
    @Autowired
    StringRedisTemplate redisTemplate;

    public List<String> scan(String pattern) {
        List<String> binaryKeys = new ArrayList<>();
        ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern).build();
        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(scanOptions)) {
            while (cursor.hasNext()) {
                binaryKeys.add(new String(cursor.next()));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return binaryKeys;
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean expire(String key, long seconds) {
        return redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    public boolean expire(String key, long time, TimeUnit timeUnit) {
        return redisTemplate.expire(key, time, timeUnit);
    }

    public long getExpire(String key) {
        return redisTemplate.opsForValue().getOperations().getExpire(key);
    }

    public void set(String key, String val, long seconds) {
        redisTemplate.opsForValue().set(key, val, seconds, TimeUnit.SECONDS);
    }

    public void set(String key, String val, long time, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, val, time, timeUnit);
    }

    public boolean setIfPresent(String key, String val, long seconds) {
        return redisTemplate.opsForValue().setIfPresent(key, val, seconds, TimeUnit.SECONDS);
    }

    public long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}
