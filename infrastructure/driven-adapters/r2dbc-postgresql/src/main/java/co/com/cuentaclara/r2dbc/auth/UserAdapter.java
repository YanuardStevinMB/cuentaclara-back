package co.com.cuentaclara.r2dbc.auth;

import co.com.cuentaclara.model.user.SystemRole;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.model.user.UserStatus;
import co.com.cuentaclara.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserAdapter implements UserGateway {

    private final UserDataRepository repository;

    @Override
    public Mono<User> save(User user) {
        UserData data = toData(user);
        return repository.save(data).map(this::toEntity);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return repository.findByEmailAndDeletedAtIsNull(email).map(this::toEntity);
    }

    @Override
    public Mono<User> findById(UUID id) {
        return repository.findById(id).map(this::toEntity);
    }

    @Override
    public Mono<User> updateLastLogin(UUID userId) {
        return repository.updateLastLogin(userId).map(this::toEntity);
    }

    private UserData toData(User user) {
        return UserData.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .passwordHash(user.getPasswordHash())
                .systemRole(user.getSystemRole().name())
                .status(user.getStatus().name())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    private User toEntity(UserData data) {
        return User.builder()
                .id(data.getId())
                .email(data.getEmail())
                .fullName(data.getFullName())
                .phone(data.getPhone())
                .passwordHash(data.getPasswordHash())
                .systemRole(SystemRole.valueOf(data.getSystemRole()))
                .status(UserStatus.valueOf(data.getStatus()))
                .lastLoginAt(data.getLastLoginAt())
                .createdAt(data.getCreatedAt())
                .updatedAt(data.getUpdatedAt())
                .deletedAt(data.getDeletedAt())
                .build();
    }
}
