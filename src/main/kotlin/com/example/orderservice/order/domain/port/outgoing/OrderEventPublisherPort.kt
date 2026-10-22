package com.example.orderservice.order.domain.port.outgoing

import com.example.orderservice.order.domain.model.OrderCreatedEvent

fun interface OrderEventPublisherPort {
	fun publish(event: OrderCreatedEvent)
}
