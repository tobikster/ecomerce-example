package com.example.orderservice.order.infrastructure.kafka.producer

import com.example.orderservice.order.domain.model.OrderCreatedEvent
import com.example.orderservice.order.domain.port.outgoing.OrderEventPublisherPort
import com.example.orderservice.order.infrastructure.kafka.dto.toDto
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderKafkaEventPublisher(
	private val kafkaTemplate: KafkaTemplate<String, Any>,
) : OrderEventPublisherPort {
	override fun publish(event: OrderCreatedEvent) {
		kafkaTemplate.send(
			ORDERS_EVENTS_TOPIC,
			event.orderId.value.toString(),
			event.toDto(),
		)
	}

	companion object {
		const val ORDERS_EVENTS_TOPIC = "orders.events"
	}
}
