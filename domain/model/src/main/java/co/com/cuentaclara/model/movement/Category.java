package co.com.cuentaclara.model.movement;

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
public class Category {
    private UUID id;
    private UUID userId;
    private String name;
    private MovementType type;
    private String icon;
    private String color;
    private boolean system;
    private boolean active;
    private Instant createdAt;
}
