# common-web

Shared Spring MVC/Web capabilities for ISC services.

## Security integration

`common-web` consumes `common-security` for JWT validation. It does not parse JWTs itself.

For an authenticated service:

```yaml
common:
  web:
    enabled: true
    security-enabled: true
    authentication-enabled: true
    protected-paths:
      - /api/**
```

For a service that only needs HTTP endpoints and must not authenticate requests (for example token-service):

```yaml
common:
  web:
    enabled: true
    security-enabled: true
    authentication-enabled: false
```

The latter still installs an explicit permit-all stateless Spring Security filter chain so Spring Boot's default security chain does not unexpectedly secure the service.

When authentication is enabled, a valid Bearer token is converted into a Spring `Authentication` whose principal is `com.isc.common.security.model.SecurityPrincipal`.

The `@CurrentUser` argument resolver can inject that principal into controller methods.
