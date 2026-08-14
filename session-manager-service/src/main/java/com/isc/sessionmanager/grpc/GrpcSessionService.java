package com.isc.sessionmanager.grpc;

import com.google.protobuf.Empty;
import com.isc.common.dto.SessionInfo;
import com.isc.common.enums.SessionReason;
import com.isc.common.enums.SessionStatus;
import com.isc.common.exception.SessionNotFoundException;
import com.isc.grpc.session.*;
import com.isc.sessionmanager.service.SessionService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;

/**
 * Wire-level adapter for SessionService. Contains no session logic
 * itself — only proto <-> domain conversion and exception mapping to
 * gRPC statuses. All actual reads/writes still go through SessionService.
 */
@GrpcService
@RequiredArgsConstructor
public class GrpcSessionService extends SessionGrpcServiceGrpc.SessionGrpcServiceImplBase {

    private final SessionService sessionService;

    @Override
    public void createSession(CreateSessionRequest request, StreamObserver<SessionIdReply> responseObserver) {
        SessionInfo session = toDomain(request.getSession());
        String sessionId = sessionService.create(session);
        responseObserver.onNext(SessionIdReply.newBuilder().setSessionId(sessionId).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getSession(SessionIdRequest request, StreamObserver<SessionReply> responseObserver) {
        SessionInfo session = sessionService.getSession(request.getSessionId());
        SessionReply.Builder reply = SessionReply.newBuilder().setFound(session != null);
        if (session != null) {
            reply.setSession(toProto(session));
        }
        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    @Override
    public void isValid(SessionIdRequest request, StreamObserver<BoolReply> responseObserver) {
        boolean valid = sessionService.isValid(request.getSessionId());
        responseObserver.onNext(BoolReply.newBuilder().setValue(valid).build());
        responseObserver.onCompleted();
    }

    @Override
    public void revokeSession(RevokeSessionRequest request, StreamObserver<Empty> responseObserver) {
        withNotFoundHandling(responseObserver, () -> {
            SessionReason reason = request.getReason().isEmpty()
                    ? SessionReason.NONE
                    : SessionReason.valueOf(request.getReason());
            sessionService.revokeSession(request.getSessionId(), reason);
        });
    }

    @Override
    public void deleteSession(SessionIdRequest request, StreamObserver<Empty> responseObserver) {
        withNotFoundHandling(responseObserver, () -> sessionService.deleteSession(request.getSessionId()));
    }

    @Override
    public void refreshSession(SessionIdRequest request, StreamObserver<Empty> responseObserver) {
        withNotFoundHandling(responseObserver, () -> sessionService.refreshSession(request.getSessionId()));
    }

    @Override
    public void touchSession(SessionIdRequest request, StreamObserver<Empty> responseObserver) {
        withNotFoundHandling(responseObserver, () -> sessionService.touchSession(request.getSessionId()));
    }

    @Override
    public void getDeviceSession(DeviceSessionRequest request, StreamObserver<SessionIdReply> responseObserver) {
        String sessionId = sessionService.getDeviceSession(request.getCustomerId(), request.getDeviceId());
        responseObserver.onNext(SessionIdReply.newBuilder().setSessionId(sessionId != null ? sessionId : "").build());
        responseObserver.onCompleted();
    }

    @Override
    public void hasActiveDeviceSession(DeviceSessionRequest request, StreamObserver<BoolReply> responseObserver) {
        boolean active = sessionService.hasActiveDeviceSession(request.getCustomerId(), request.getDeviceId());
        responseObserver.onNext(BoolReply.newBuilder().setValue(active).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getClientSession(ClientIdRequest request, StreamObserver<SessionIdReply> responseObserver) {
        String sessionId = sessionService.getClientSession(request.getClientId());
        responseObserver.onNext(SessionIdReply.newBuilder().setSessionId(sessionId != null ? sessionId : "").build());
        responseObserver.onCompleted();
    }

    // ---------------------------------------------------------------
    // Conversion & error mapping
    // ---------------------------------------------------------------

    private void withNotFoundHandling(StreamObserver<Empty> responseObserver, Runnable action) {
        try {
            action.run();
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (SessionNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        }
    }

    private SessionInfo toDomain(com.isc.grpc.session.SessionInfo proto) {
        return SessionInfo.builder()
                .sessionId(proto.getSessionId())
                .customerId(proto.getCustomerId())
                .deviceId(proto.getDeviceId())
                .clientId(proto.getClientId())
                .username(proto.getUsername())
                .ipAddress(proto.getIpAddress())
                .node(proto.getNode())
                .protocol(proto.getProtocol())
                .refreshTokenId(proto.getRefreshTokenId())
                .createdAt(toInstant(proto.getCreatedAt()))
                .expireAt(toInstant(proto.getExpireAt()))
                .lastRefreshAt(toInstant(proto.getLastRefreshAt()))
                .lastEventTimestamp(proto.getLastEventTimestamp() != 0 ? proto.getLastEventTimestamp() : null)
                .status(proto.getStatus().isEmpty() ? null : SessionStatus.valueOf(proto.getStatus()))
                .reason(proto.getReason().isEmpty() ? SessionReason.NONE : SessionReason.valueOf(proto.getReason()))
                .build();
    }

    private com.isc.grpc.session.SessionInfo toProto(SessionInfo session) {
        com.isc.grpc.session.SessionInfo.Builder builder = com.isc.grpc.session.SessionInfo.newBuilder()
                .setSessionId(nullToEmpty(session.getSessionId()))
                .setCustomerId(nullToEmpty(session.getCustomerId()))
                .setDeviceId(nullToEmpty(session.getDeviceId()))
                .setClientId(nullToEmpty(session.getClientId()))
                .setUsername(nullToEmpty(session.getUsername()))
                .setIpAddress(nullToEmpty(session.getIpAddress()))
                .setNode(nullToEmpty(session.getNode()))
                .setRefreshTokenId(nullToEmpty(session.getRefreshTokenId()))
                .setCreatedAt(toEpochMilli(session.getCreatedAt()))
                .setExpireAt(toEpochMilli(session.getExpireAt()))
                .setLastRefreshAt(toEpochMilli(session.getLastRefreshAt()))
                .setStatus(session.getStatus() != null ? session.getStatus().name() : "")
                .setReason(session.getReason() != null ? session.getReason().name() : "");

        if (session.getProtocol() != null) {
            builder.setProtocol(session.getProtocol());
        }
        if (session.getLastEventTimestamp() != null) {
            builder.setLastEventTimestamp(session.getLastEventTimestamp());
        }
        return builder.build();
    }

    private Instant toInstant(long epochMilli) {
        return epochMilli != 0 ? Instant.ofEpochMilli(epochMilli) : null;
    }

    private long toEpochMilli(Instant instant) {
        return instant != null ? instant.toEpochMilli() : 0L;
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
