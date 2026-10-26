package com.example.paymentservice.account.domain.port.incoming

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.CustomerId
import com.example.paymentservice.account.domain.model.Money
import java.math.BigDecimal

interface DepositUseCase {
	operator fun invoke(command: DepositCommand): Account
}

data class DepositCommand(
	val customerId: CustomerId,
	val funds: Money,
) {
	init {
		require(funds.amount > BigDecimal.ZERO) { "Deposit amount must be greater than zero." }
	}
}
