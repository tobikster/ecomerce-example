package com.example.paymentservice.account.application.service

import arrow.core.Either
import arrow.core.raise.either
import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.WithdrawFailure
import com.example.paymentservice.account.domain.port.incoming.WithdrawCommand
import com.example.paymentservice.account.domain.port.incoming.WithdrawUseCase
import com.example.paymentservice.account.domain.port.outgoing.AccountRepositoryPort
import org.springframework.stereotype.Service

@Service
class WithdrawService(
	private val accountRepository: AccountRepositoryPort,
) : WithdrawUseCase {
	override fun invoke(command: WithdrawCommand): Either<WithdrawFailure, Account> =
		either {
			val account =
				accountRepository.findByCustomerIdAndCurrency(command.customerId, command.funds.currency)
					?: Account.create(command.customerId, command.funds.currency)

			account
				.withdraw(command.funds)
				.onRight(accountRepository::save)
				.bind()
		}
}
