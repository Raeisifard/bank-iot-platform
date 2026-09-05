package com.isc.sessionmanager.service;

import com.isc.common.dto.SessionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal, read-only API so other services (e.g. the message-dispatch /
 * acknowledge REST API) can check whether a given session is currently
 * online, and on which EMQX node.
 *
 * Not intended to be internet-facing — put behind internal network / gateway ACLs.
 *
 * Sessions are keyed by sessionId (jwt.sid) in a Redis hash (see
 * SessionServiceImpl), so this endpoint reads through the same SessionService
 * used everywhere else in this module. If a caller needs to resolve phone
 * number -> sessionId, that lookup belongs upstream (token-service owns that
 * mapping), not here.
 */
@RestController
@RequestMapping("/internal/sessions")
@RequiredArgsConstructor
public class SessionQueryController {

    private final SessionService sessionService;

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionInfo> getSession(@PathVariable String sessionId) {
        SessionInfo session = sessionService.getSession(sessionId);
        return session != null
                ? ResponseEntity.ok(session)
                : ResponseEntity.notFound().build();
    }
}
