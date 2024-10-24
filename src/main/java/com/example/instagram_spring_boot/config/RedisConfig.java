package com.example.instagram_spring_boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

//어떻게 작동하는지 흐름을 아는 것이 중요하다.
@Configuration
public class RedisConfig {

    //Value어노테이션을 통해 application.property에서 정의한 host와 port를 적용시켜주어야 한다.
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    //Bean을 통해서 redis 연결함. 아까 Value 어노테이션으로 선언한 host와 port를 사용했음
    @Bean
    public RedisConnectionFactory redisConnectionFactoryForString() {
        return new LettuceConnectionFactory(host, port);
    }

    //기본 name인 redisTemplate가 없으면 오류가 발생함(왜인지는 아직 모름). 아마도 RedisTemplate가 기본 어쩌고인데
    //name으로 redisTemplate를 해둔 것이 없으면 그걸 찾지 못해서 오류를 발생시키는 것 같음.
    //커넥션 시켜주고, 키랑 값 받을 수 있게 세팅함. [Type].class 로 값의 타입 정해줌
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
