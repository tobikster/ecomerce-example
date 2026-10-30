package com.example.paymentservice.payment.infrastructure.kafka

import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.OrderId
import com.example.paymentservice.common.domain.model.of
import com.example.paymentservice.payment.domain.port.incoming.ProcessOrderPaymentCommand
import com.example.paymentservice.payment.domain.port.incoming.ProcessOrderPaymentUseCase
import com.example.paymentservice.payment.infrastructure.kafka.dto.OrderCreatedEventDto
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class OrderEventsKafkaConsumer(
	private val jsonMapper: JsonMapper,
	private val processOrderPayment: ProcessOrderPaymentUseCase,
) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@KafkaListener(topics = [ORDERS_TOPIC])
	fun handleOrderEvent(
		@Payload payload: String,
		@Header("eventType", required = false) eventType: String?,
		@Header("eventId", required = false) eventId: String?,
		@Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
	) {
		logger.info(
			"Received Kafka record from topic: {}, eventType: {}, eventId: {}, key: {}",
			ORDERS_TOPIC,
			eventType,
			eventId,
			key,
		)

		if (eventType == EVENT_TYPE_ORDER_CREATED) {
			val message: OrderCreatedEventDto = jsonMapper.readValue(payload, OrderCreatedEventDto::class.java)
			logger.info(
				"Successfully parsed OrderCreatedMessage: orderId={}, amount: {} {}",
				message.orderId,
				message.amount,
				message.currency,
			)
			processOrderPayment(
				ProcessOrderPaymentCommand(
					orderId = OrderId(message.orderId),
					customerId = CustomerId(message.customerId),
					amount = message.amount of message.currency,
				),
			)
		} else {
			logger.warn("Ignoring unknown or missing eventType: '{}'", eventType)
		}
	}

	companion object {
		const val ORDERS_TOPIC = "orders.events"
		const val EVENT_TYPE_ORDER_CREATED = "OrderCreated"
	}
}
