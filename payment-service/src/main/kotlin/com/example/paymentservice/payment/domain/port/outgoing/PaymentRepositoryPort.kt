package com.example.paymentservice.payment.domain.port.outgoing

import com.example.paymentservice.common.domain.model.OrderId
import com.example.paymentservice.payment.domain.model.Payment

interface PaymentRepositoryPort {
	fun findByOrderId(orderId: OrderId): Payment?

	fun save(payment: Payment): Payment
}
