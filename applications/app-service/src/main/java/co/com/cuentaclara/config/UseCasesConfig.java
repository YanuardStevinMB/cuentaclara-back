package co.com.cuentaclara.config;

import co.com.cuentaclara.model.audit.gateways.AuditLogGateway;
import co.com.cuentaclara.model.auth.gateways.AuthTokenGateway;
import co.com.cuentaclara.model.auth.gateways.JwtGateway;
import co.com.cuentaclara.model.auth.gateways.PasswordHashGateway;
import co.com.cuentaclara.model.auth.gateways.PasswordResetGateway;
import co.com.cuentaclara.model.user.gateways.PrivacyConsentGateway;
import co.com.cuentaclara.model.user.gateways.UserGateway;
import co.com.cuentaclara.model.user.gateways.UserPreferenceGateway;
import co.com.cuentaclara.model.dashboard.gateways.DashboardGateway;
import co.com.cuentaclara.model.movement.gateways.CategoryGateway;
import co.com.cuentaclara.model.movement.gateways.MovementGateway;
import co.com.cuentaclara.model.contact.gateways.ContactGateway;
import co.com.cuentaclara.model.loan.gateways.LoanGateway;
import co.com.cuentaclara.model.budget.gateways.BudgetGateway;
import co.com.cuentaclara.usecase.contact.CreateContactUseCase;
import co.com.cuentaclara.usecase.contact.GetContactsUseCase;
import co.com.cuentaclara.usecase.contact.ToggleContactStatusUseCase;
import co.com.cuentaclara.usecase.contact.UpdateContactUseCase;
import co.com.cuentaclara.usecase.loan.CreateLoanUseCase;
import co.com.cuentaclara.usecase.loan.GetLoansUseCase;
import co.com.cuentaclara.usecase.loan.GetLoanSummaryUseCase;
import co.com.cuentaclara.usecase.loan.ManagePaymentsUseCase;
import co.com.cuentaclara.usecase.loan.UpdateLoanUseCase;
import co.com.cuentaclara.usecase.budget.CreateBudgetUseCase;
import co.com.cuentaclara.usecase.budget.GetBudgetsUseCase;
import co.com.cuentaclara.usecase.budget.UpdateBudgetUseCase;
import co.com.cuentaclara.usecase.auth.ForgotPasswordUseCase;
import co.com.cuentaclara.usecase.auth.LoginUseCase;
import co.com.cuentaclara.usecase.auth.LogoutUseCase;
import co.com.cuentaclara.usecase.auth.RegisterUserUseCase;
import co.com.cuentaclara.usecase.auth.ResetPasswordUseCase;
import co.com.cuentaclara.usecase.auth.ValidateTokenUseCase;
import co.com.cuentaclara.usecase.dashboard.GetAlertsUseCase;
import co.com.cuentaclara.usecase.dashboard.GetDashboardSummaryUseCase;
import co.com.cuentaclara.usecase.dashboard.GetExpensesByCategoryUseCase;
import co.com.cuentaclara.usecase.dashboard.GetMonthlyFlowUseCase;
import co.com.cuentaclara.usecase.dashboard.GetUpcomingPaymentsUseCase;
import co.com.cuentaclara.usecase.movement.CreateMovementUseCase;
import co.com.cuentaclara.usecase.movement.DeleteMovementUseCase;
import co.com.cuentaclara.usecase.movement.GetMovementsUseCase;
import co.com.cuentaclara.usecase.movement.GetMovementSummaryUseCase;
import co.com.cuentaclara.usecase.movement.ManageCategoryUseCase;
import co.com.cuentaclara.usecase.movement.UpdateMovementUseCase;
import co.com.cuentaclara.usecase.profile.ChangePasswordUseCase;
import co.com.cuentaclara.usecase.profile.DeactivateAccountUseCase;
import co.com.cuentaclara.usecase.profile.GetProfileUseCase;
import co.com.cuentaclara.usecase.profile.SavePrivacyConsentUseCase;
import co.com.cuentaclara.usecase.profile.UpdatePreferencesUseCase;
import co.com.cuentaclara.usecase.profile.UpdateProfileUseCase;
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

    @Bean
    public GetProfileUseCase getProfileUseCase(UserGateway userGateway,
                                               UserPreferenceGateway preferenceGateway) {
        return new GetProfileUseCase(userGateway, preferenceGateway);
    }

    @Bean
    public UpdateProfileUseCase updateProfileUseCase(UserGateway userGateway,
                                                     AuditLogGateway auditLogGateway) {
        return new UpdateProfileUseCase(userGateway, auditLogGateway);
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(UserGateway userGateway,
                                                       PasswordHashGateway passwordHashGateway,
                                                       AuditLogGateway auditLogGateway) {
        return new ChangePasswordUseCase(userGateway, passwordHashGateway, auditLogGateway);
    }

    @Bean
    public UpdatePreferencesUseCase updatePreferencesUseCase(UserPreferenceGateway preferenceGateway) {
        return new UpdatePreferencesUseCase(preferenceGateway);
    }

    @Bean
    public SavePrivacyConsentUseCase savePrivacyConsentUseCase(PrivacyConsentGateway consentGateway) {
        return new SavePrivacyConsentUseCase(consentGateway);
    }

    @Bean
    public DeactivateAccountUseCase deactivateAccountUseCase(UserGateway userGateway,
                                                              AuditLogGateway auditLogGateway) {
        return new DeactivateAccountUseCase(userGateway, auditLogGateway);
    }

    @Bean
    public GetDashboardSummaryUseCase getDashboardSummaryUseCase(DashboardGateway dashboardGateway) {
        return new GetDashboardSummaryUseCase(dashboardGateway);
    }

    @Bean
    public GetMonthlyFlowUseCase getMonthlyFlowUseCase(DashboardGateway dashboardGateway) {
        return new GetMonthlyFlowUseCase(dashboardGateway);
    }

    @Bean
    public GetExpensesByCategoryUseCase getExpensesByCategoryUseCase(DashboardGateway dashboardGateway) {
        return new GetExpensesByCategoryUseCase(dashboardGateway);
    }

    @Bean
    public GetUpcomingPaymentsUseCase getUpcomingPaymentsUseCase(DashboardGateway dashboardGateway) {
        return new GetUpcomingPaymentsUseCase(dashboardGateway);
    }

    @Bean
    public GetAlertsUseCase getAlertsUseCase(DashboardGateway dashboardGateway) {
        return new GetAlertsUseCase(dashboardGateway);
    }

    @Bean
    public CreateMovementUseCase createMovementUseCase(MovementGateway movementGateway, CategoryGateway categoryGateway) {
        return new CreateMovementUseCase(movementGateway, categoryGateway);
    }

    @Bean
    public UpdateMovementUseCase updateMovementUseCase(MovementGateway movementGateway) {
        return new UpdateMovementUseCase(movementGateway);
    }

    @Bean
    public DeleteMovementUseCase deleteMovementUseCase(MovementGateway movementGateway) {
        return new DeleteMovementUseCase(movementGateway);
    }

    @Bean
    public GetMovementsUseCase getMovementsUseCase(MovementGateway movementGateway) {
        return new GetMovementsUseCase(movementGateway);
    }

    @Bean
    public GetMovementSummaryUseCase getMovementSummaryUseCase(MovementGateway movementGateway) {
        return new GetMovementSummaryUseCase(movementGateway);
    }

    @Bean
    public ManageCategoryUseCase manageCategoryUseCase(CategoryGateway categoryGateway) {
        return new ManageCategoryUseCase(categoryGateway);
    }

    // ===== CONTACTS =====

    @Bean
    public CreateContactUseCase createContactUseCase(ContactGateway contactGateway) {
        return new CreateContactUseCase(contactGateway);
    }

    @Bean
    public UpdateContactUseCase updateContactUseCase(ContactGateway contactGateway) {
        return new UpdateContactUseCase(contactGateway);
    }

    @Bean
    public GetContactsUseCase getContactsUseCase(ContactGateway contactGateway) {
        return new GetContactsUseCase(contactGateway);
    }

    @Bean
    public ToggleContactStatusUseCase toggleContactStatusUseCase(ContactGateway contactGateway) {
        return new ToggleContactStatusUseCase(contactGateway);
    }

    // ===== LOANS =====

    @Bean
    public CreateLoanUseCase createLoanUseCase(LoanGateway loanGateway) {
        return new CreateLoanUseCase(loanGateway);
    }

    @Bean
    public GetLoansUseCase getLoansUseCase(LoanGateway loanGateway) {
        return new GetLoansUseCase(loanGateway);
    }

    @Bean
    public UpdateLoanUseCase updateLoanUseCase(LoanGateway loanGateway) {
        return new UpdateLoanUseCase(loanGateway);
    }

    @Bean
    public ManagePaymentsUseCase managePaymentsUseCase(LoanGateway loanGateway) {
        return new ManagePaymentsUseCase(loanGateway);
    }

    @Bean
    public GetLoanSummaryUseCase getLoanSummaryUseCase(LoanGateway loanGateway) {
        return new GetLoanSummaryUseCase(loanGateway);
    }

    // ===== BUDGETS =====

    @Bean
    public CreateBudgetUseCase createBudgetUseCase(BudgetGateway budgetGateway) {
        return new CreateBudgetUseCase(budgetGateway);
    }

    @Bean
    public GetBudgetsUseCase getBudgetsUseCase(BudgetGateway budgetGateway) {
        return new GetBudgetsUseCase(budgetGateway);
    }

    @Bean
    public UpdateBudgetUseCase updateBudgetUseCase(BudgetGateway budgetGateway) {
        return new UpdateBudgetUseCase(budgetGateway);
    }
}
