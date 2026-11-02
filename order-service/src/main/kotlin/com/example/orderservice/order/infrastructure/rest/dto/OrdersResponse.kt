package com.example.orderservice.order.infrastructure.rest.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response containing list of orders")
data class OrdersResponse(
	@field:Schema(description = "List of orders")
	val orders: List<OrderResponse>,
)
