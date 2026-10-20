package com.example.orderservice.order.domain.port.incoming

import com.example.orderservice.order.domain.model.CustomerId

interface GetCustomersUseCase {
	operator fun invoke(): List<CustomerId>
}
