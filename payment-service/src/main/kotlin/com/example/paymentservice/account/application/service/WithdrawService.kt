package com.example.paymentservice.account.application.service

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.port.incoming.WithdrawCommand
import com.example.paymentservice.account.domain.port.incoming.WithdrawUseCase
import com.example.paymentservice.account.domain.port.outgoing.AccountRepositoryPort
import org.springframework.stereotype.Service

@Service
class WithdrawService(
	private val accountRepository: AccountRepositoryPort,
) : WithdrawUseCase {
	override fun invoke(command: WithdrawCommand): Account {
		val account =
			accountRepository.findByCustomerIdAndCurrency(command.customerId, command.funds.currency)
				?: Account.create(command.customerId, command.funds.currency)

		val updatedAccount = account.withdraw(command.funds)
		return accountRepository.save(updatedAccount)
	}
}
