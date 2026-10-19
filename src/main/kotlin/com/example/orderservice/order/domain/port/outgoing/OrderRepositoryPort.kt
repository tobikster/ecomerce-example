package com.example.orderservice.order.domain.port.outgoing

import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.OrderId

interface OrderRepositoryPort {
	fun save(order: Order): Order

	fun findById(id: OrderId): Order?
}
