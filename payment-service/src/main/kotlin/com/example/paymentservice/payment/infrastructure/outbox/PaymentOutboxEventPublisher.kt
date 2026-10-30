package com.example.paymentservice.payment.infrastructure.outbox

import com.example.common.outbox.infrastructure.outbox.OutboxEvent
import com.example.common.outbox.infrastructure.outbox.OutboxRepository
import com.example.paymentservice.payment.domain.model.PaymentEvent
import com.example.paymentservice.payment.domain.port.outgoing.PaymentOutboxPublisherPort
import com.example.paymentservice.payment.infrastructure.outbox.dto.toDto
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class PaymentOutboxEventPublisher(
	private val outboxRepository: OutboxRepository,
	private val jsonMapper: JsonMapper,
) : PaymentOutboxPublisherPort {
	override fun publish(event: PaymentEvent) {
		val (eventType: String, payload: String) =
			when (event) {
				is PaymentEvent.Completed -> EVENT_TYPE_PAYMENT_COMPLETED to jsonMapper.writeValueAsString(event.toDto())
				is PaymentEvent.Failed -> EVENT_TYPE_PAYMENT_FAILED to jsonMapper.writeValueAsString(event.toDto())
			}

		val outboxEvent =
			OutboxEvent(
				aggregateId = event.orderId.value.toString(),
				topic = TOPIC,
				eventType = eventType,
				createdAt = event.createdAt,
				payload = payload,
			)
		
		outboxRepository.save(outboxEvent)
	}

	companion object {
		const val TOPIC = "payments.events"
		const val EVENT_TYPE_PAYMENT_COMPLETED = "PaymentCompleted"
		const val EVENT_TYPE_PAYMENT_FAILED = "PaymentFailed"
	}
}
