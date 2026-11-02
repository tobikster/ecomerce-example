package com.example.orderservice.order.domain.port.incoming

import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.OrderId

interface GetOrderUseCase {
	operator fun invoke(query: GetOrderQuery): Order?
}

data class GetOrderQuery(
	val id: OrderId,
)
