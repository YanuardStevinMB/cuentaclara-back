package co.com.cuentaclara.usecase.contact;

import co.com.cuentaclara.model.contact.Contact;
import co.com.cuentaclara.model.contact.gateways.ContactGateway;
import co.com.cuentaclara.model.exception.BusinessException;
import co.com.cuentaclara.model.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateContactUseCase {
    private final ContactGateway contactGateway;

    public Mono<Contact> execute(Contact contact) {
        return contactGateway.existsByNameAndPhone(contact.getUserId(), contact.getFullName(), contact.getPhone())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new BusinessException(ErrorCode.CONTACT_DUPLICATE));
                    }
                    contact.setId(UUID.randomUUID());
                    contact.setActive(true);
                    contact.setCreatedAt(Instant.now());
                    contact.setUpdatedAt(Instant.now());
                    return contactGateway.save(contact);
                });
    }
}
