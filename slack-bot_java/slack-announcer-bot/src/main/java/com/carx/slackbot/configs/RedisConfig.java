package com.carx.slackbot.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;

/**
 * @author Created by KarpuninVD on 17.01.2023
 */
@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}") private String host;
    @Value("${spring.data.redis.port}") private int port;

    @Bean
    public RedisStandaloneConfiguration redisConfiguration() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        return config;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory(redisConfiguration());
    }

    @Bean
    public RedisTemplate<String, Integer> redisTemplate() {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new RedisSerializer<Integer>() {
            @Override
            public byte[] serialize(Integer integer) throws SerializationException {
                return integer.toString().getBytes();
            }

            @Override
            public Integer deserialize(byte[] bytes) throws SerializationException {
                return Integer.parseInt(new String(bytes));
            }
        });
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
}
