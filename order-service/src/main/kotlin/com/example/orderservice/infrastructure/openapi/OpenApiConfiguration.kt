package com.example.orderservice.infrastructure.openapi

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration {
	@Bean
	fun orderServiceOpenApi(): OpenAPI =
		OpenAPI()
			.info(
				Info()
					.title("Order Service API")
					.description("REST API of Order Service")
					.version("0.0.1"),
			)
}
