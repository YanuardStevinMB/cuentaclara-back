package co.com.cuentaclara.usecase.auth;

import co.com.cuentaclara.model.auth.PasswordResetToken;
import co.com.cuentaclara.model.auth.gateways.PasswordResetGateway;
import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import co.com.cuentaclara.model.user.UserStatus;
import co.com.cuentaclara.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RequiredArgsConstructor
public class ForgotPasswordUseCase {

    private final UserGateway userGateway;
    private final PasswordResetGateway passwordResetGateway;

    public Mono<String> execute(String email) {
        return userGateway.findByEmail(email.toLowerCase().trim())
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .flatMap(user -> {
                    String tokenValue = UUID.randomUUID().toString();
                    PasswordResetToken token = PasswordResetToken.builder()
                            .id(UUID.randomUUID())
                            .userId(user.getId())
                            .tokenHash(tokenValue)
                            .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                            .createdAt(Instant.now())
                            .build();
                    return passwordResetGateway.save(token)
                            .thenReturn(tokenValue);
                })
                .switchIfEmpty(Mono.just("OK"));
    }
}
