package com.isc.useridentity.api;

import com.isc.useridentity.domain.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

public final class ApiDtos {
    private ApiDtos() {}
    public record CreateUserRequest(@NotBlank String userId, @NotBlank String customerId) {}
    public record UserResponse(String userId,String customerId,UserStatus status,AssuranceLevel assuranceLevel) {}
    public record StartAuthenticationRequest(@NotBlank String userId,@NotBlank String clientId,@NotNull AuthenticationPurpose purpose,@NotNull AssuranceLevel requiredAssuranceLevel) {}
    public record StepUpRequest(@NotNull AssuranceLevel requiredAssuranceLevel,@NotNull AuthenticationMethod method) {}
    public record OtpChallengeResponse(String challengeId,Instant expiresAt,long retryAfterSeconds) {}
    public record OtpVerifyRequest(@NotBlank String challengeId,@NotBlank String code) {}
    public record BiometricVerifyRequest(@NotBlank String assertion) {}
    public record AuthenticationContext(String authenticationId,String userId,String clientId,AuthenticationPurpose purpose,AuthenticationMethod authenticationMethod,AssuranceLevel assuranceLevel,Instant authenticatedAt,Instant expiresAt) {}
}
