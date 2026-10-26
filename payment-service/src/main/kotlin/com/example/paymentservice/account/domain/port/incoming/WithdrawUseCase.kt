package com.example.paymentservice.account.domain.port.incoming

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.CustomerId
import com.example.paymentservice.account.domain.model.Money

interface WithdrawUseCase {
	operator fun invoke(command: WithdrawCommand): Account
}

data class WithdrawCommand(
	val customerId: CustomerId,
	val funds: Money,
)
