package com.example.instagram_spring_boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactoryForString() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, String> redisString(RedisConnectionFactory redisConnectionFactoryForString) {
        RedisTemplate<String, String> redisString = new RedisTemplate<>();
        redisString.setConnectionFactory(redisConnectionFactoryForString);
        redisString.setKeySerializer(new StringRedisSerializer());
        redisString.setValueSerializer(new org.springframework.data.redis.serializer.GenericToStringSerializer<>(String.class));

        return redisString;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactoryForByte() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Byte> redisByte(RedisConnectionFactory redisConnectionFactoryForByte) {
        RedisTemplate<String, Byte> redisString = new RedisTemplate<>();
        redisString.setConnectionFactory(redisConnectionFactoryForByte);
        redisString.setKeySerializer(new StringRedisSerializer());
        redisString.setValueSerializer(new org.springframework.data.redis.serializer.GenericToStringSerializer<>(Byte.class));

        return redisString;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactoryForLong() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean(name = "redisTemplateLong")
    public RedisTemplate<String, Long> redisLong(RedisConnectionFactory redisConnectionFactoryForLong) {
        RedisTemplate<String, Long> redisLong = new RedisTemplate<>();
        redisLong.setConnectionFactory(redisConnectionFactoryForLong);
        redisLong.setKeySerializer(new StringRedisSerializer());
        redisLong.setValueSerializer(new org.springframework.data.redis.serializer.GenericToStringSerializer<>(Byte.class));

        return redisLong;
    }
}
