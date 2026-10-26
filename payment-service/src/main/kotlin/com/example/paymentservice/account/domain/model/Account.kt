package com.example.paymentservice.account.domain.model

import java.time.Instant
import java.util.Currency
import java.util.UUID

data class Account private constructor(
	val id: AccountId,
	val customerId: CustomerId,
	val balance: Money,
	val createdAt: Instant = Instant.now(),
	val updatedAt: Instant = createdAt,
) {
	fun deposit(funds: Money): Account {
		require(balance.currency == funds.currency) {
			"Currency mismatch: cannot deposit ${funds.currency} to account with ${balance.currency}!"
		}

		return copy(
			balance = balance + funds,
			updatedAt = Instant.now(),
		)
	}

	fun withdraw(funds: Money): Account {
		require(balance.currency == funds.currency) {
			"Currency mismatch: cannot withdraw ${funds.currency} from account with ${balance.currency}!"
		}

		if (balance.amount < funds.amount) {
			throw AccountException.InsufficientFunds(requested = funds, available = balance)
		}

		return copy(
			balance = balance - funds,
			updatedAt = Instant.now(),
		)
	}

	companion object {
		fun create(
			customerId: CustomerId,
			currency: Currency,
		) = Account(
			id = AccountId(),
			customerId = customerId,
			balance = Money.zero(currency),
		)

		fun restore(
			id: AccountId,
			customerId: CustomerId,
			balance: Money,
			createdAt: Instant = Instant.now(),
			updatedAt: Instant = createdAt,
		) = Account(
			id = id,
			customerId = customerId,
			balance = balance,
			createdAt = createdAt,
			updatedAt = updatedAt,
		)
	}
}

@JvmInline
value class AccountId(
	val value: UUID = UUID.randomUUID(),
)

@JvmInline
value class CustomerId(
	val value: String,
) {
	init {
		require(value.isNotBlank()) { "Customer ID cannot be blank" }
	}
}
