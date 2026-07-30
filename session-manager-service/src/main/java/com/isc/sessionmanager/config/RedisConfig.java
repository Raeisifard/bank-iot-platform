package com.isc.sessionmanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.sessionmanager.model.ClientSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, ClientSession> sessionRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {

        RedisTemplate<String, ClientSession> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<ClientSession> valueSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, ClientSession.class);

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
