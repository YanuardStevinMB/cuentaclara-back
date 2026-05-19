package co.com.cuentaclara.model.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAlert {
    private String type;
    private String title;
    private String description;
    private String severity;
    private String actionUrl;
}
