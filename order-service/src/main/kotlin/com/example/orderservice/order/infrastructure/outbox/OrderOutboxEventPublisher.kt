package com.example.orderservice.order.infrastructure.outbox

import com.example.common.outbox.infrastructure.outbox.OutboxEvent
import com.example.common.outbox.infrastructure.outbox.OutboxRepository
import com.example.orderservice.order.domain.model.OrderCreatedEvent
import com.example.orderservice.order.domain.port.outgoing.OrderEventPublisherPort
import com.example.orderservice.order.infrastructure.kafka.dto.toDto
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class OrderOutboxEventPublisher(
	private val outboxRepository: OutboxRepository,
	private val jsonMapper: JsonMapper,
) : OrderEventPublisherPort {
	override fun publish(event: OrderCreatedEvent) {
		val payload: String = event.toDto().let(jsonMapper::writeValueAsString)
		val outboxEvent =
			OutboxEvent(
				aggregateId = event.orderId.value.toString(),
				topic = TOPIC,
				eventType = EVENT_TYPE_ORDER_CREATED,
				payload = payload,
			)

		outboxRepository.save(outboxEvent)
	}

	companion object {
		const val TOPIC = "orders.events"
		const val EVENT_TYPE_ORDER_CREATED = "OrderCreated"
	}
}
