package com.carx.slackbot.repo;

import com.carx.slackbot.configs.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * @author Created by KarpuninVD on 17.01.2023
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PublishedEventRepo {
    private final RedisTemplate<String, Integer> redisTemplate;

    public void save(int msgId, AppProperties.Inform inform) {
        redisTemplate.opsForSet().add("time:"+inform.getTime(), msgId);
        log.info("Added  time:{} - {} to DB", inform.getTime(), msgId);
    }

    public boolean hasByMessageId(int msgId, AppProperties.Inform inform) {
        log.info("Check existing time:{} - {}", inform.getTime(), msgId);
        return Objects.requireNonNull(redisTemplate.opsForSet().members("time:"+inform.getTime())).contains(msgId);
    }

    public void clearAll() {
        final Set<String> keys = redisTemplate.keys("*");
        Objects.requireNonNull(keys).forEach(key -> redisTemplate.delete(key));
    }
}