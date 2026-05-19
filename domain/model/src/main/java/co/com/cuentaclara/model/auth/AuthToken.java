package co.com.cuentaclara.model.auth;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AuthToken {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;
}
