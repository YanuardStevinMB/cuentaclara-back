package co.com.cuentaclara.api.auth;

import co.com.cuentaclara.model.auth.AuthToken;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.usecase.auth.ForgotPasswordUseCase;
import co.com.cuentaclara.usecase.auth.LoginUseCase;
import co.com.cuentaclara.usecase.auth.LogoutUseCase;
import co.com.cuentaclara.usecase.auth.RegisterUserUseCase;
import co.com.cuentaclara.usecase.auth.ResetPasswordUseCase;
import co.com.cuentaclara.usecase.auth.ValidateTokenUseCase;
import co.com.cuentaclara.api.auth.dto.LoginRequest;
import co.com.cuentaclara.api.auth.dto.RegisterRequest;
import co.com.cuentaclara.api.auth.dto.ForgotPasswordRequest;
import co.com.cuentaclara.api.auth.dto.ResetPasswordRequest;
import co.com.cuentaclara.api.auth.dto.AuthTokenResponse;
import co.com.cuentaclara.api.auth.dto.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ValidateTokenUseCase validateTokenUseCase;

    @PostMapping("/register")
    public Mono<ResponseEntity<MessageResponse>> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return registerUserUseCase.execute(
                        request.fullName(),
                        request.email(),
                        request.password(),
                        ipAddress,
                        userAgent)
                .map(user -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new MessageResponse("User registered successfully")));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthTokenResponse>> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return loginUseCase.execute(request.email(), request.password(), ipAddress, userAgent)
                .map(token -> ResponseEntity.ok(toResponse(token)));
    }

    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<MessageResponse>> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {
        return forgotPasswordUseCase.execute(request.email())
                .map(result -> ResponseEntity.ok(
                        new MessageResponse("If the email exists, a reset link has been sent")));
    }

    @PostMapping("/reset-password")
    public Mono<ResponseEntity<MessageResponse>> resetPassword(
            @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return resetPasswordUseCase.execute(request.token(), request.newPassword(), ipAddress, userAgent)
                .then(Mono.just(ResponseEntity.ok(new MessageResponse("Password reset successfully"))));
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<MessageResponse>> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {
        String token = authHeader.replace("Bearer ", "");
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return validateTokenUseCase.execute(token)
                .flatMap(user -> logoutUseCase.execute(user.getId(), ipAddress, userAgent))
                .then(Mono.just(ResponseEntity.ok(new MessageResponse("Logged out successfully"))));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private AuthTokenResponse toResponse(AuthToken token) {
        return new AuthTokenResponse(
                token.getAccessToken(),
                token.getRefreshToken(),
                token.getExpiresIn(),
                token.getTokenType());
    }
}
