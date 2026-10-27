package com.example.paymentservice.account.domain.port.incoming

import com.example.paymentservice.account.domain.model.WithdrawResult
import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.Money

interface WithdrawUseCase {
	operator fun invoke(command: WithdrawCommand): WithdrawResult
}

data class WithdrawCommand(
	val customerId: CustomerId,
	val funds: Money,
)
