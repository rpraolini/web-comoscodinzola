package it.asso.core.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

import it.asso.core.multitenancy.TenantContext;

public class TenantFilter implements Filter {

    private static final String DEFAULT_TENANT_ID = "asso";

    // Lista dei tenant validi
    private static final Set<String> VALID_TENANTS = new HashSet<>(Arrays.asList(
            "asso", "demo", "mariano"
    ));

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String tenantId = DEFAULT_TENANT_ID;

        try {
            String hostHeader = req.getHeader("Host");

            if (hostHeader != null) {
                // 1. Rimuovi la porta (se presente)
                if (hostHeader.contains(":")) {
                    hostHeader = hostHeader.substring(0, hostHeader.indexOf(":"));
                }

                // 2. Tenta di estrarre il primo segmento
                // Esempio: "asso.local" -> ["asso", "local"]
                String[] segments = hostHeader.split("\\.");

                // Se ci sono almeno 2 segmenti (e.g., asso . local)
                if (segments.length >= 2) {
                    String potentialTenant = segments[0]; // Questo è 'asso', 'demo', ecc.
                    //System.out.println("tenant = " + potentialTenant);

                    if (VALID_TENANTS.contains(potentialTenant)) {
                        tenantId = potentialTenant;
                    }
                }
            }

            TenantContext.setCurrentTenant(tenantId);
            chain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }
}