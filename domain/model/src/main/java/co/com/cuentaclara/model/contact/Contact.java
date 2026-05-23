package co.com.cuentaclara.model.contact;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Contact {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String phone;
    private String email;
    private String notes;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    // Transient
    private int activeLoansCount;
    private int totalLoansCount;
}
