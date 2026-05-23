package co.com.cuentaclara.usecase.contact;

import co.com.cuentaclara.model.contact.Contact;
import co.com.cuentaclara.model.contact.gateways.ContactGateway;
import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RequiredArgsConstructor
public class UpdateContactUseCase {
    private final ContactGateway contactGateway;

    public Mono<Contact> execute(Contact contact) {
        return contactGateway.findById(contact.getId())
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.CONTACT_NOT_FOUND)))
                .flatMap(existing -> {
                    existing.setFullName(contact.getFullName());
                    existing.setPhone(contact.getPhone());
                    existing.setEmail(contact.getEmail());
                    existing.setNotes(contact.getNotes());
                    existing.setUpdatedAt(Instant.now());
                    return contactGateway.update(existing);
                });
    }
}
