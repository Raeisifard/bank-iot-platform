package com.isc.kafka.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.common.constants.KafkaTopics;
import com.isc.common.dto.ClientSession;
import com.isc.common.enums.SessionStatus;
import com.isc.contract.event.session.ClientConnectedEvent;
import com.isc.kafka.session.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientConnectedConsumer {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AppProperties props;

    @KafkaListener(
            topics = KafkaTopics.MQTT_CONNECTED,
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ClientConnectedEvent event) {

        try {

            ClientSession session =
                    ClientSession.builder()
                            .sid(event.getJwt().getSid())
                            .jti(event.getJwt().getJti())
                            .clientId(event.getClientId())
                            .username(event.getUsername())
                            .deviceId(event.getJwt().getDid())
                            .channelId(event.getJwt().getCid())
                            .issuer(event.getJwt().getIss())
                            .audience(event.getJwt().getAud())
                            .protocol(event.getProtocol())
                            .ipAddress(event.getIpAddress())
                            .status(SessionStatus.ONLINE)
                            .connectedAt(event.getTimestamp())
                            .lastSeenAt(event.getTimestamp())
                            .build();

            String sessionKey =
                    props.getRedis().getSessionPrefix()
                            + event.getJwt().getSid();

            redisTemplate.opsForValue().set(
                    sessionKey,
                    objectMapper.writeValueAsString(session),
                    Duration.ofHours(
                            props.getRedis().getDefaultTtlHours()));

            redisTemplate.opsForSet().add(
                    props.getRedis().getClientSessionsPrefix()
                            + event.getClientId(),
                    event.getJwt().getSid());

            redisTemplate.opsForSet().add(
                    props.getRedis().getOnlineUsersKey(),
                    event.getClientId());

            log.info(
                    "Client [{}] connected, session [{}] registered",
                    event.getClientId(),
                    event.getJwt().getSid());

        } catch (Exception ex) {

            log.error(
                    "Failed to process connect event for client {}",
                    event.getClientId(),
                    ex);
        }
    }
}