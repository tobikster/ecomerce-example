package com.example.paymentservice.payment.infrastructure.persistence.mapper

import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.OrderId
import com.example.paymentservice.common.domain.model.of
import com.example.paymentservice.payment.domain.model.Payment
import com.example.paymentservice.payment.domain.model.PaymentId
import com.example.paymentservice.payment.infrastructure.persistence.entity.PaymentEntity

fun Payment.toEntity() =
	PaymentEntity(
		id = id.value,
		orderId = orderId.value,
		customerId = customerId.value,
		amount = amount.amount,
		currency = amount.currency,
		status = status,
		failureReason = failureReason,
		createdAt = createdAt,
	)

fun PaymentEntity.toDomain() =
	Payment.restore(
		id = PaymentId(id),
		orderId = OrderId(orderId),
		customerId = CustomerId(customerId),
		amount = amount of currency,
		status = status,
		failureReason = failureReason,
		createdAt = createdAt,
	)
