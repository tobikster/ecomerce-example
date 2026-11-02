package com.example.orderservice.order.application.service

import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.OrderFailure
import com.example.orderservice.order.domain.model.PaymentId
import com.example.orderservice.order.domain.port.incoming.ProcessPaymentCommand
import com.example.orderservice.order.domain.port.incoming.ProcessPaymentFailure
import com.example.orderservice.order.domain.port.incoming.ProcessPaymentUseCase
import com.example.orderservice.order.domain.port.outgoing.OrderRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProcessPaymentService(
	private val orderRepository: OrderRepositoryPort,
) : ProcessPaymentUseCase {
	@Transactional
	override fun invoke(command: ProcessPaymentCommand) =
		either {
			val order =
				orderRepository.findById(command.orderId)
					?: raise(ProcessPaymentFailure.OrderNotFound(command.orderId))

			val updatedOrder =
				order
					.withPaymentResult(command.isSuccessful, command.paymentId)
					.mapLeft(::handleOrderFailure)
					.bind()

			orderRepository.save(updatedOrder)
		}

	private fun Order.withPaymentResult(
		isPaymentSuccessful: Boolean,
		paymentId: PaymentId,
	) = if (isPaymentSuccessful) {
		withConfirmedPayment(paymentId)
	} else {
		withFailedPayment(paymentId)
	}

	private fun handleOrderFailure(failure: OrderFailure): ProcessPaymentFailure =
		when (failure) {
			is OrderFailure.InvalidStatus -> {
				ProcessPaymentFailure.InvalidOrderStatus(
					failure.status,
				)
			}
		}
}
