package co.com.cuentaclara.model.contact.gateways;

import co.com.cuentaclara.model.contact.Contact;
import co.com.cuentaclara.model.contact.FinancialHistoryItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ContactGateway {
    Mono<Contact> save(Contact contact);
    Mono<Contact> findById(UUID id);
    Flux<Contact> findByUserId(UUID userId);
    Flux<Contact> findActiveByUserId(UUID userId);
    Flux<Contact> searchByNameOrPhone(UUID userId, String query);
    Mono<Contact> update(Contact contact);
    Mono<Void> toggleStatus(UUID id, boolean active);
    Mono<Boolean> existsByNameAndPhone(UUID userId, String fullName, String phone);
    Flux<FinancialHistoryItem> getFinancialHistory(UUID contactId);
}
