package it.asso.core.config; // Adatta il package

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.asso.core.filter.TenantFilter; // Assicurati che questo import sia corretto

// Assumi che questa classe contenga già i bean dei tuoi Data Source e del TenantRoutingDataSource
@Configuration
public class TenantFilterRegistrationConfig {

    /**
     * Registra il TenantFilter e ne definisce l'ordine di esecuzione.
     */
    @Bean
    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration() {
        FilterRegistrationBean<TenantFilter> registrationBean = new FilterRegistrationBean<>();

        // 1. Specifica la classe del filtro
        registrationBean.setFilter(new TenantFilter());

        // 2. Mappa il filtro a tutti gli URL
        registrationBean.addUrlPatterns("/*");

        // 3. Imposta la priorità (deve essere molto alta/bassa in valore,
        // per essere eseguito prima di Spring Security)
        // Spring Security è di solito attorno a 0; usiamo -100 per assicurarci la precedenza.
        registrationBean.setOrder(-100);

        return registrationBean;
    }
}