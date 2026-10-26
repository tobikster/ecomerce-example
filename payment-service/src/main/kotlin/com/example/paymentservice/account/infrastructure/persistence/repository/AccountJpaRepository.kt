package com.example.paymentservice.account.infrastructure.persistence.repository

import com.example.paymentservice.account.infrastructure.persistence.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Currency
import java.util.UUID

interface AccountJpaRepository : JpaRepository<AccountEntity, UUID> {
	fun findByCustomerIdAndCurrency(
		customerId: String,
		currency: Currency,
	): AccountEntity?
}
