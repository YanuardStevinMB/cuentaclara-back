package co.com.cuentaclara.usecase.contact;

import co.com.cuentaclara.model.contact.gateways.ContactGateway;
import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class ToggleContactStatusUseCase {
    private final ContactGateway contactGateway;

    public Mono<Void> execute(UUID contactId, boolean active) {
        return contactGateway.findById(contactId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.CONTACT_NOT_FOUND)))
                .flatMap(contact -> contactGateway.toggleStatus(contactId, active));
    }
}
