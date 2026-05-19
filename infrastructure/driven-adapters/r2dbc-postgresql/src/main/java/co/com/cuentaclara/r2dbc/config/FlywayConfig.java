package co.com.cuentaclara.r2dbc.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(PostgresqlConnectionProperties properties) {
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s",
                properties.host(), properties.port(), properties.database());
        return Flyway.configure()
                .dataSource(jdbcUrl, properties.username(), properties.password())
                .schemas(properties.schema())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
    }
}
