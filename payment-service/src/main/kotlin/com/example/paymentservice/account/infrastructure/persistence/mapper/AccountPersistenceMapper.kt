package com.example.paymentservice.account.infrastructure.persistence.mapper

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.AccountId
import com.example.paymentservice.account.domain.model.CustomerId
import com.example.paymentservice.account.domain.model.of
import com.example.paymentservice.account.infrastructure.persistence.entity.AccountEntity

fun Account.toEntity() =
	AccountEntity(
		id = id.value,
		customerId = customerId.value,
		balance = balance.amount,
		currency = balance.currency,
		createdAt = createdAt,
		updatedAt = updatedAt,
	)

fun AccountEntity.toDomain() =
	Account.restore(
		id = AccountId(id),
		customerId = CustomerId(customerId),
		balance = balance of currency,
		createdAt = createdAt,
		updatedAt = updatedAt,
	)
