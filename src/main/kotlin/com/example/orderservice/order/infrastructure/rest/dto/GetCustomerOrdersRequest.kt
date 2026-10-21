package com.example.orderservice.order.infrastructure.rest.dto

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.port.incoming.GetCustomerOrdersQuery
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class GetCustomerOrdersRequest(
	@field:NotBlank
	@field:Size(max = 64)
	val customerId: String = "",
)

fun GetCustomerOrdersRequest.toQuery() =
	GetCustomerOrdersQuery(
		customerId = CustomerId(customerId),
	)
