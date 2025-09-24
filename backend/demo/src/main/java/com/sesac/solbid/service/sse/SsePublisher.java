package com.sesac.solbid.service.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SsePublisher {

    private final StringRedisTemplate redis;
    private final ObjectMapper om = new ObjectMapper();

    public void publish(String channel, Object payload) {
        try {
            String json = om.writeValueAsString(payload);
            redis.convertAndSend(channel, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
