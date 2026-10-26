package com.example.paymentservice.account.infrastructure.persistence.adapter

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.CustomerId
import com.example.paymentservice.account.domain.port.outgoing.AccountRepositoryPort
import com.example.paymentservice.account.infrastructure.persistence.mapper.toDomain
import com.example.paymentservice.account.infrastructure.persistence.mapper.toEntity
import com.example.paymentservice.account.infrastructure.persistence.repository.AccountJpaRepository
import org.springframework.stereotype.Repository
import java.util.Currency

@Repository
class AccountPersistenceAdapter(
	private val repository: AccountJpaRepository,
) : AccountRepositoryPort {
	override fun findByCustomerIdAndCurrency(
		customerId: CustomerId,
		currency: Currency,
	) = repository.findByCustomerIdAndCurrency(customerId.value, currency)?.toDomain()

	override fun save(account: Account) = repository.save(account.toEntity()).toDomain()
}
