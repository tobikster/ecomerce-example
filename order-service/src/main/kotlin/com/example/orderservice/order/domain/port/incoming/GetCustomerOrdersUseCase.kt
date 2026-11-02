package com.example.orderservice.order.domain.port.incoming

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.Order

interface GetCustomerOrdersUseCase {
	operator fun invoke(query: GetCustomerOrdersQuery): List<Order>
}

data class GetCustomerOrdersQuery(
	val customerId: CustomerId,
)
