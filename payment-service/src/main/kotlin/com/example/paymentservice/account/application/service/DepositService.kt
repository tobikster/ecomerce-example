package com.example.paymentservice.account.application.service

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.port.incoming.DepositCommand
import com.example.paymentservice.account.domain.port.incoming.DepositUseCase
import com.example.paymentservice.account.domain.port.outgoing.AccountRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DepositService(
	private val accountRepository: AccountRepositoryPort,
) : DepositUseCase {
	@Transactional
	override fun invoke(command: DepositCommand): Account {
		val account =
			accountRepository.findByCustomerIdAndCurrency(command.customerId, command.funds.currency)
				?: Account.create(command.customerId, command.funds.currency)

		val updatedAccount = account.deposit(command.funds)
		return accountRepository.save(updatedAccount)
	}
}
