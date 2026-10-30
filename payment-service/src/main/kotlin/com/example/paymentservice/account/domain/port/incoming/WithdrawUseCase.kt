package com.example.paymentservice.account.domain.port.incoming

import arrow.core.Either
import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.WithdrawFailure
import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.Money

interface WithdrawUseCase {
	operator fun invoke(command: WithdrawCommand): Either<WithdrawFailure, Account>
}

data class WithdrawCommand(
	val customerId: CustomerId,
	val funds: Money,
)
