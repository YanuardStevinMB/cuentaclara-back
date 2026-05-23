package co.com.cuentaclara.api.contact;

import co.com.cuentaclara.model.contact.Contact;
import co.com.cuentaclara.model.contact.FinancialHistoryItem;
import co.com.cuentaclara.model.user.User;
import co.com.cuentaclara.usecase.auth.ValidateTokenUseCase;
import co.com.cuentaclara.usecase.contact.CreateContactUseCase;
import co.com.cuentaclara.usecase.contact.GetContactsUseCase;
import co.com.cuentaclara.usecase.contact.ToggleContactStatusUseCase;
import co.com.cuentaclara.usecase.contact.UpdateContactUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/contacts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ContactController {

    private final ValidateTokenUseCase validateTokenUseCase;
    private final CreateContactUseCase createContactUseCase;
    private final UpdateContactUseCase updateContactUseCase;
    private final GetContactsUseCase getContactsUseCase;
    private final ToggleContactStatusUseCase toggleContactStatusUseCase;

    @PostMapping
    public Mono<ResponseEntity<Contact>> create(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ContactRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    Contact contact = Contact.builder()
                            .userId(user.getId())
                            .fullName(request.fullName())
                            .phone(request.phone())
                            .email(request.email())
                            .notes(request.notes())
                            .build();
                    return createContactUseCase.execute(contact);
                })
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c));
    }

    @GetMapping
    public Mono<ResponseEntity<List<Contact>>> list(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    if (search != null && !search.isBlank()) {
                        return getContactsUseCase.search(user.getId(), search).collectList();
                    }
                    if (Boolean.TRUE.equals(activeOnly)) {
                        return getContactsUseCase.getActiveByUser(user.getId()).collectList();
                    }
                    return getContactsUseCase.getAllByUser(user.getId()).collectList();
                })
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Contact>> getById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {
        return extractUser(authHeader)
                .flatMap(user -> getContactsUseCase.getById(id))
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Contact>> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody ContactRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> {
                    Contact contact = Contact.builder()
                            .id(id)
                            .userId(user.getId())
                            .fullName(request.fullName())
                            .phone(request.phone())
                            .email(request.email())
                            .notes(request.notes())
                            .build();
                    return updateContactUseCase.execute(contact);
                })
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/status")
    public Mono<ResponseEntity<Void>> toggleStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @RequestBody StatusRequest request) {
        return extractUser(authHeader)
                .flatMap(user -> toggleContactStatusUseCase.execute(id, request.active()))
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @GetMapping("/{id}/financial-history")
    public Mono<ResponseEntity<List<FinancialHistoryItem>>> getFinancialHistory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {
        return extractUser(authHeader)
                .flatMap(user -> getContactsUseCase.getFinancialHistory(id).collectList())
                .map(ResponseEntity::ok);
    }

    private Mono<User> extractUser(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return validateTokenUseCase.execute(token);
    }

    // ===== DTOs =====

    public record ContactRequest(
            String fullName,
            String phone,
            String email,
            String notes
    ) {}

    public record StatusRequest(boolean active) {}
}
