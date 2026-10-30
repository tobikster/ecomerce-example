package com.example.orderservice.order.infrastructure.kafka

import com.example.orderservice.order.domain.model.OrderId
import com.example.orderservice.order.domain.model.PaymentId
import com.example.orderservice.order.domain.port.incoming.ProcessPaymentCommand
import com.example.orderservice.order.domain.port.incoming.ProcessPaymentFailure
import com.example.orderservice.order.domain.port.incoming.ProcessPaymentUseCase
import com.example.orderservice.order.infrastructure.kafka.dto.PaymentCompletedEventDto
import com.example.orderservice.order.infrastructure.kafka.dto.PaymentFailedEventDto
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class PaymentEventsKafkaConsumer(
	private val jsonMapper: JsonMapper,
	private val processPayment: ProcessPaymentUseCase,
) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@KafkaListener(topics = [PAYMENTS_TOPIC])
	fun handlePaymentEvent(
		@Payload payload: String,
		@Header("eventType", required = false) eventType: String?,
		@Header("eventId", required = false) eventId: String?,
		@Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
	) {
		logger.info(
			"Received Kafka record from topic: {}, eventType: {}, eventId: {}, key: {}",
			PAYMENTS_TOPIC,
			eventType,
			eventId,
			key,
		)

		when (eventType) {
			EVENT_TYPE_PAYMENT_COMPLETED -> {
				val message: PaymentCompletedEventDto =
					jsonMapper.readValue(payload, PaymentCompletedEventDto::class.java)
				logger.info(
					"Successfully parsed PAYMENT_COMPLETED event: paymentId={}, orderId={}, amount={} {}",
					message.paymentId,
					message.orderId,
					message.amount,
					message.currency,
				)

				handlePayment(
					ProcessPaymentCommand(
						orderId = OrderId(message.orderId),
						paymentId = PaymentId(message.paymentId),
						isSuccessful = true,
					),
				)
			}

			EVENT_TYPE_PAYMENT_FAILED -> {
				val message: PaymentFailedEventDto =
					jsonMapper.readValue(payload, PaymentFailedEventDto::class.java)
				logger.info(
					"Successfully parsed PAYMENT_FAILED event: paymentId={}, orderId={}, amount={} {}, reason: {}",
					message.paymentId,
					message.orderId,
					message.amount,
					message.currency,
					message.failureReason,
				)

				handlePayment(
					ProcessPaymentCommand(
						orderId = OrderId(message.orderId),
						paymentId = PaymentId(message.paymentId),
						isSuccessful = false,
					),
				)
			}

			else -> {
				logger.warn("Ignoring unknown or missing eventType: '{}'", eventType)
			}
		}
	}

	private fun handlePayment(command: ProcessPaymentCommand) {
		processPayment(command).fold(
			ifLeft = { failure ->
				when (failure) {
					is ProcessPaymentFailure.OrderNotFound -> {
						logger.error(
							"Order {} not found when processing payment {}",
							command.orderId,
							command.paymentId,
						)
						error("Order ${command.orderId} not found when processing payment ${command.paymentId}")
					}

					is ProcessPaymentFailure.InvalidOrderStatus -> {
						logger.warn(
							"Could not process payment {} for order {}: order is in invalid status {}",
							command.paymentId,
							command.orderId,
							failure.status,
						)
					}
				}
			},
			ifRight = { order ->
				logger.info(
					"Order {} status successfully updated to {}",
					order.id,
					order.status,
				)
			},
		)
	}

	companion object {
		const val PAYMENTS_TOPIC = "payments.events"
		const val EVENT_TYPE_PAYMENT_COMPLETED = "PaymentCompleted"
		const val EVENT_TYPE_PAYMENT_FAILED = "PaymentFailed"
	}
}
