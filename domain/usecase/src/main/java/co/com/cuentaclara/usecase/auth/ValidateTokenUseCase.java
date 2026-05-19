package co.com.cuentaclara.usecase.auth;

import co.com.cuentaclara.model.auth.gateways.JwtGateway;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class ValidateTokenUseCase {

    private final JwtGateway jwtGateway;
    private final UserGateway userGateway;

    public Mono<User> execute(String accessToken) {
        return jwtGateway.validateAccessToken(accessToken)
                .flatMap(userGateway::findById);
    }
}
