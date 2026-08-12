package com.isc.sessionmanager.service;

import com.isc.common.dto.ClientSession;
import com.isc.sessionmanager.repository.SessionRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal, read-only API so other services (e.g. the message-dispatch /
 * acknowledge REST API) can check whether a given phone number currently
 * has an active EMQX session, and on which node.
 *
 * Not intended to be internet-facing — put behind internal network / gateway ACLs.
 */
@RestController
@RequestMapping("/internal/sessions")
@RequiredArgsConstructor
public class SessionQueryController {

    private final SessionRedisRepository sessionRepository;

    @GetMapping("/{phoneNumber}")
    public ResponseEntity<ClientSession> getSession(@PathVariable String phoneNumber) {
        return sessionRepository.find(phoneNumber)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
