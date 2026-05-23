package co.com.cuentaclara.usecase.contact;

import co.com.cuentaclara.model.contact.Contact;
import co.com.cuentaclara.model.contact.FinancialHistoryItem;
import co.com.cuentaclara.model.contact.gateways.ContactGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class GetContactsUseCase {
    private final ContactGateway contactGateway;

    public Flux<Contact> getAllByUser(UUID userId) {
        return contactGateway.findByUserId(userId);
    }

    public Flux<Contact> getActiveByUser(UUID userId) {
        return contactGateway.findActiveByUserId(userId);
    }

    public Mono<Contact> getById(UUID id) {
        return contactGateway.findById(id);
    }

    public Flux<Contact> search(UUID userId, String query) {
        return contactGateway.searchByNameOrPhone(userId, query);
    }

    public Flux<FinancialHistoryItem> getFinancialHistory(UUID contactId) {
        return contactGateway.getFinancialHistory(contactId);
    }
}
