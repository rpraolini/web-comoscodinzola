package it.asso.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "spring.datasource")
public class MultiTenantDataSourceProperties {
    private Map<String, DataSourceProperties> tenants = new HashMap<>();

    public Map<String, DataSourceProperties> getTenants() {
        return tenants;
    }

    public void setTenants(Map<String, DataSourceProperties> tenants) {
        this.tenants = tenants;
    }

    public static class DataSourceProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private Map<String, String> hikari = new HashMap<>();

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Map<String, String> getHikari() {
            return hikari;
        }

        public void setHikari(Map<String, String> hikari) {
            this.hikari = hikari;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
