package com.isc.contract.event.session;

import com.isc.contract.event.BaseKafkaEvent;
import com.isc.contract.event.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientConnectedEvent extends BaseKafkaEvent {

    private Long timestamp;
    private String clientId;
    private String username;
    private String ipAddress;
    private Integer protocol;
    private JwtInfo jwt;
}