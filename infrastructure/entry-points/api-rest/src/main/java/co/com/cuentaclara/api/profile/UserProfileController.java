package co.com.cuentaclara.api.profile;

import co.com.cuentaclara.api.auth.dto.MessageResponse;
import co.com.cuentaclara.api.profile.dto.ChangePasswordRequest;
import co.com.cuentaclara.api.profile.dto.DeactivateAccountRequest;
import co.com.cuentaclara.api.profile.dto.PrivacyConsentRequest;
import co.com.cuentaclara.api.profile.dto.ProfileResponse;
import co.com.cuentaclara.api.profile.dto.UpdatePreferencesRequest;
import co.com.cuentaclara.api.profile.dto.UpdateProfileRequest;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.model.user.UserPreference;
import co.com.cuentaclara.usecase.auth.ValidateTokenUseCase;
import co.com.cuentaclara.usecase.profile.ChangePasswordUseCase;
import co.com.cuentaclara.usecase.profile.DeactivateAccountUseCase;
import co.com.cuentaclara.usecase.profile.GetProfileUseCase;
import co.com.cuentaclara.usecase.profile.SavePrivacyConsentUseCase;
import co.com.cuentaclara.usecase.profile.UpdatePreferencesUseCase;
import co.com.cuentaclara.usecase.profile.UpdateProfileUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/users/me", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserProfileController {

    private final ValidateTokenUseCase validateTokenUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UpdatePreferencesUseCase updatePreferencesUseCase;
    private final SavePrivacyConsentUseCase savePrivacyConsentUseCase;
    private final DeactivateAccountUseCase deactivateAccountUseCase;

    @GetMapping
    public Mono<ResponseEntity<ProfileResponse>> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        return extractUser(authHeader)
                .flatMap(user -> getProfileUseCase.execute(user.getId()))
                .map(result -> ResponseEntity.ok(toProfileResponse(result.user(), result.preference())));
    }

    @PutMapping
    public Mono<ResponseEntity<ProfileResponse>> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return extractUser(authHeader)
                .flatMap(user -> updateProfileUseCase.execute(user.getId(), request.fullName(), request.phone(), ip))
                .flatMap(user -> getProfileUseCase.execute(user.getId()))
                .map(result -> ResponseEntity.ok(toProfileResponse(result.user(), result.preference())));
    }

    @PatchMapping("/password")
    public Mono<ResponseEntity<MessageResponse>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return extractUser(authHeader)
                .flatMap(user -> changePasswordUseCase.execute(
                        user.getId(), request.currentPassword(), request.newPassword(), ip))
                .then(Mono.just(ResponseEntity.ok(new MessageResponse("Password changed successfully"))));
    }

    @PatchMapping("/preferences")
    public Mono<ResponseEntity<ProfileResponse.PreferenceResponse>> updatePreferences(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdatePreferencesRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> updatePreferencesUseCase.execute(
                        user.getId(), request.currency(), request.dateFormat()))
                .map(pref -> ResponseEntity.ok(
                        new ProfileResponse.PreferenceResponse(pref.getCurrency(), pref.getDateFormat())));
    }

    @PostMapping("/privacy-consent")
    public Mono<ResponseEntity<MessageResponse>> saveConsent(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PrivacyConsentRequest request,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return extractUser(authHeader)
                .flatMap(user -> savePrivacyConsentUseCase.execute(
                        user.getId(), request.policyVersion(), request.accepted(), ip))
                .map(consent -> ResponseEntity.ok(new MessageResponse("Privacy consent recorded")));
    }

    @DeleteMapping
    public Mono<ResponseEntity<MessageResponse>> deactivateAccount(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody DeactivateAccountRequest request,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return extractUser(authHeader)
                .flatMap(user -> deactivateAccountUseCase.execute(
                        user.getId(), request.confirmationText(), ip))
                .then(Mono.just(ResponseEntity.ok(new MessageResponse("Account deactivated successfully"))));
    }

    private Mono<User> extractUser(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return validateTokenUseCase.execute(token);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private ProfileResponse toProfileResponse(User user, UserPreference pref) {
        return new ProfileResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getStatus().name(),
                user.getSystemRole().name(),
                new ProfileResponse.PreferenceResponse(
                        pref.getCurrency(),
                        pref.getDateFormat()
                )
        );
    }
}
