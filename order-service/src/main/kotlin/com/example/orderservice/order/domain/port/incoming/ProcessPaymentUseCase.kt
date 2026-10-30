package com.example.orderservice.order.domain.port.incoming

import arrow.core.Either
import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.OrderId
import com.example.orderservice.order.domain.model.OrderStatus
import com.example.orderservice.order.domain.model.PaymentId

interface ProcessPaymentUseCase {
	operator fun invoke(command: ProcessPaymentCommand): Either<ProcessPaymentFailure, Order>
}

data class ProcessPaymentCommand(
	val orderId: OrderId,
	val paymentId: PaymentId,
	val isSuccessful: Boolean,
)

sealed interface ProcessPaymentFailure {
	data class OrderNotFound(
		val orderId: OrderId,
	) : ProcessPaymentFailure

	data class InvalidOrderStatus(
		val status: OrderStatus,
	) : ProcessPaymentFailure
}
