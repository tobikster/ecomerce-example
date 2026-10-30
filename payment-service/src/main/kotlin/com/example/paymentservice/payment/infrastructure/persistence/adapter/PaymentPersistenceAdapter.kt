package com.example.paymentservice.payment.infrastructure.persistence.adapter

import com.example.paymentservice.common.domain.model.OrderId
import com.example.paymentservice.payment.domain.model.Payment
import com.example.paymentservice.payment.domain.port.outgoing.PaymentRepositoryPort
import com.example.paymentservice.payment.infrastructure.persistence.mapper.toDomain
import com.example.paymentservice.payment.infrastructure.persistence.mapper.toEntity
import com.example.paymentservice.payment.infrastructure.persistence.repository.PaymentJpaRepository
import org.springframework.stereotype.Repository

@Repository
class PaymentPersistenceAdapter(
	private val repository: PaymentJpaRepository,
) : PaymentRepositoryPort {
	override fun findByOrderId(orderId: OrderId): Payment? = repository.findByOrderId(orderId.value)?.toDomain()

	override fun save(payment: Payment) = repository.save(payment.toEntity()).toDomain()
}
