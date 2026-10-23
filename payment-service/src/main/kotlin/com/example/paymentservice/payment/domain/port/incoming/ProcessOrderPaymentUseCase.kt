package com.example.paymentservice.payment.domain.port.incoming

import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.Money
import com.example.paymentservice.common.domain.model.OrderId

interface ProcessOrderPaymentUseCase {
	operator fun invoke(command: ProcessOrderPaymentCommand)
}

data class ProcessOrderPaymentCommand(
	val orderId: OrderId,
	val customerId: CustomerId,
	val amount: Money,
)
