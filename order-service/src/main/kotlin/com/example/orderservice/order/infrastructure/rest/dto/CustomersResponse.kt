package com.example.orderservice.order.infrastructure.rest.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response containing unique customer identifiers")
data class CustomersResponse(
	@field:Schema(
		description = "List of unique customer identifiers",
		example = "[\"customer_123\", \"customer_456\"]",
	)
	val customers: List<String>,
)
