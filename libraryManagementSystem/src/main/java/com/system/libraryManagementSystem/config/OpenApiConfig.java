package com.system.libraryManagementSystem.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Aaron Callanga",
                        email = "aaroncallanga01@gmail.com",
                        url = "https://www.linkedin.com/in/aaroncallanga/"
                ),
                description = "OpenApi Documentation for Library Management System Project",
                title = "Library Management System",
                version = "1.0",
                license = @License(
                        name = "Example License Name",
                        url = "https://www.linkedin.com/in/aaroncallanga/"
                ),
                termsOfService = "Terms of Service"     //must be a link
        ),
        servers = {         //Servers to test API in different environment, list of servers where the request would go to. ex: use GET and you can choose what server(environment) to send that request
                @Server(
                        description = "Local Environment",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Production Environment",
                        url = "https://test.yourdomain.com"
                ),
                @Server(
                        description = "Test Environment",
                        url = "https://api.yourdomain.com"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"             //apply the security scheme globally, use @Operation(summary = "sample summary", security = @SecurityRequirement(name = "")) for overriding for public points
                )
        }
)
@SecurityScheme(            //you can have many security scheme, but we only have jwt right now so we declare this security scheme and pick this one too
        name = "bearerAuth",
        description = "JWT authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

}
