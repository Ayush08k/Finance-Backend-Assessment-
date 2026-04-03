package com.finance.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Finance Data Processing & Access Control API",
        version = "1.0.0",
        description = "Backend API for a finance dashboard system with role-based access control. " +
                      "Roles: VIEWER (dashboard only), ANALYST (read records + trends), ADMIN (full access).",
        contact = @Contact(name = "Finance Backend")
    )
)
@SecurityScheme(
    name = "BearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Enter your JWT token from POST /api/auth/login"
)
public class OpenApiConfig {
}
