package it.asso.core.multitenancy;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    // Spring chiama questo metodo per ottenere la chiave (tenant ID)
    // che userà per cercare nella Map definita in DataSourceConfig.java
    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }
}