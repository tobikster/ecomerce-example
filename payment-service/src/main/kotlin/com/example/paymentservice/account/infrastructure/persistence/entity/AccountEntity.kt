package com.example.paymentservice.account.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency
import java.util.UUID

@Entity
@Table(name = "accounts")
class AccountEntity(
	@Id
	@Column(name = "id", nullable = false, updatable = false)
	val id: UUID,
	@Column(name = "customer_id", nullable = false)
	var customerId: String,
	@Column(name = "balance", nullable = false)
	var balance: BigDecimal,
	@Column(name = "currency", nullable = false)
	var currency: Currency,
	@Column(name = "created_at", nullable = false)
	val createdAt: Instant,
	@Column(name = "updated_at", nullable = false)
	var updatedAt: Instant,
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is AccountEntity) return false
		return id == other.id
	}

	override fun hashCode(): Int = id.hashCode()

	override fun toString() =
		"AccountEntity(" +
			"id=$id" +
			"customerId=$customerId" +
			"balance=$balance" +
			"currency=$currency" +
			"createdAt=$createdAt" +
			"updatedAt=$updatedAt" +
			")"
}
