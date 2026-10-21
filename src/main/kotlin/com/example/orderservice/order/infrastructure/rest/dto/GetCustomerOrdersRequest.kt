package com.example.orderservice.order.infrastructure.rest.dto

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.port.incoming.GetCustomerOrdersQuery
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springdoc.core.annotations.ParameterObject

@ParameterObject
@Schema(description = "Query parameters for filtering customer orders")
data class GetCustomerOrdersRequest(
	@field:NotBlank
	@field:Size(max = 64)
	@field:Schema(description = "Customer id", example = "customer_123", requiredMode = Schema.RequiredMode.REQUIRED)
	val customerId: String = "",
)

fun GetCustomerOrdersRequest.toQuery() =
	GetCustomerOrdersQuery(
		customerId = CustomerId(customerId),
	)
