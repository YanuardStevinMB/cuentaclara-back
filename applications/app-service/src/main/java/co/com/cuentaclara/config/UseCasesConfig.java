package co.com.cuentaclara.config;

import co.com.cuentaclara.model.audit.gateways.AuditLogGateway;
import co.com.cuentaclara.model.auth.gateways.AuthTokenGateway;
import co.com.cuentaclara.model.auth.gateways.JwtGateway;
import co.com.cuentaclara.model.auth.gateways.PasswordHashGateway;
import co.com.cuentaclara.model.auth.gateways.PasswordResetGateway;
import co.com.cuentaclara.model.user.gateways.UserGateway;
import co.com.cuentaclara.usecase.auth.ForgotPasswordUseCase;
import co.com.cuentaclara.usecase.auth.LoginUseCase;
import co.com.cuentaclara.usecase.auth.LogoutUseCase;
import co.com.cuentaclara.usecase.auth.RegisterUserUseCase;
import co.com.cuentaclara.usecase.auth.ResetPasswordUseCase;
import co.com.cuentaclara.usecase.auth.ValidateTokenUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserGateway userGateway,
                                                    PasswordHashGateway passwordHashGateway,
                                                    AuditLogGateway auditLogGateway) {
        return new RegisterUserUseCase(userGateway, passwordHashGateway, auditLogGateway);
    }

    @Bean
    public LoginUseCase loginUseCase(UserGateway userGateway,
                                     PasswordHashGateway passwordHashGateway,
                                     JwtGateway jwtGateway,
                                     AuthTokenGateway authTokenGateway,
                                     AuditLogGateway auditLogGateway) {
        return new LoginUseCase(userGateway, passwordHashGateway, jwtGateway, authTokenGateway, auditLogGateway);
    }

    @Bean
    public ForgotPasswordUseCase forgotPasswordUseCase(UserGateway userGateway,
                                                       PasswordResetGateway passwordResetGateway) {
        return new ForgotPasswordUseCase(userGateway, passwordResetGateway);
    }

    @Bean
    public ResetPasswordUseCase resetPasswordUseCase(PasswordResetGateway passwordResetGateway,
                                                     UserGateway userGateway,
                                                     PasswordHashGateway passwordHashGateway,
                                                     AuditLogGateway auditLogGateway) {
        return new ResetPasswordUseCase(passwordResetGateway, userGateway, passwordHashGateway, auditLogGateway);
    }

    @Bean
    public LogoutUseCase logoutUseCase(AuthTokenGateway authTokenGateway,
                                       AuditLogGateway auditLogGateway) {
        return new LogoutUseCase(authTokenGateway, auditLogGateway);
    }

    @Bean
    public ValidateTokenUseCase validateTokenUseCase(JwtGateway jwtGateway,
                                                     UserGateway userGateway) {
        return new ValidateTokenUseCase(jwtGateway, userGateway);
    }
}
