package co.com.cuentaclara.usecase.auth;

import co.com.cuentaclara.model.audit.AuditLog;
import co.com.cuentaclara.model.audit.gateways.AuditLogGateway;
import co.com.cuentaclara.model.auth.gateways.PasswordHashGateway;
import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import co.com.cuentaclara.model.user.SystemRole;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.model.user.UserStatus;
import co.com.cuentaclara.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserGateway userGateway;
    private final PasswordHashGateway passwordHashGateway;
    private final AuditLogGateway auditLogGateway;

    private static final int MIN_PASSWORD_LENGTH = 8;

    public Mono<User> execute(String fullName, String email, String rawPassword, String ipAddress, String userAgent) {
        return validatePassword(rawPassword)
                .then(userGateway.findByEmail(email))
                .flatMap(existing -> Mono.<User>error(new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS)))
                .switchIfEmpty(Mono.defer(() -> createUser(fullName, email, rawPassword)))
                .flatMap(user -> registerAudit(user, ipAddress, userAgent).thenReturn(user));
    }

    private Mono<Void> validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            return Mono.error(new BusinessException(ErrorCode.WEAK_PASSWORD));
        }
        return Mono.empty();
    }

    private Mono<User> createUser(String fullName, String email, String rawPassword) {
        String hashedPassword = passwordHashGateway.hash(rawPassword);
        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName(fullName)
                .email(email.toLowerCase().trim())
                .passwordHash(hashedPassword)
                .systemRole(SystemRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return userGateway.save(user);
    }

    private Mono<AuditLog> registerAudit(User user, String ipAddress, String userAgent) {
        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .action("REGISTER")
                .entityType("USER")
                .entityId(user.getId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(Instant.now())
                .build();
        return auditLogGateway.save(log);
    }
}
