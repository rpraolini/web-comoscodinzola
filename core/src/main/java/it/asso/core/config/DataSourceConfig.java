package it.asso.core.config; // Adatta il package

import com.zaxxer.hikari.HikariDataSource;
import it.asso.core.multitenancy.TenantRoutingDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(MultiTenantDataSourceProperties.class)
public class DataSourceConfig {

    @Bean
    public DataSource tenantAwareDataSource(MultiTenantDataSourceProperties properties) {
        Map<Object, Object> dataSources = new HashMap<>();

        properties.getTenants().forEach((tenantId, dsProps) -> {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(dsProps.getUrl());
            ds.setUsername(dsProps.getUsername());
            ds.setPassword(dsProps.getPassword());
            ds.setDriverClassName(dsProps.getDriverClassName());

            // Configura Hikari se presente
            dsProps.getHikari().forEach(ds::addDataSourceProperty);

            dataSources.put(tenantId, ds);
        });

        TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource();
        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.setDefaultTargetDataSource(dataSources.get("asso"));
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }
}