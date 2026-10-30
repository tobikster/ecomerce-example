package com.example.paymentservice.payment.infrastructure.persistence.repository

import com.example.paymentservice.payment.infrastructure.persistence.entity.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PaymentJpaRepository : JpaRepository<PaymentEntity, UUID> {
	fun findByOrderId(orderId: UUID): PaymentEntity?
}
