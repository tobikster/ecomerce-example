package com.example.paymentservice.account.application.service

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.port.incoming.GetAccountsQuery
import com.example.paymentservice.account.domain.port.incoming.GetAccountsUseCase
import com.example.paymentservice.account.domain.port.outgoing.AccountRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetAccountService(
	private val accountRepository: AccountRepositoryPort,
) : GetAccountsUseCase {
	override fun invoke(query: GetAccountsQuery): List<Account> = accountRepository.findByCustomerId(query.customerId)
}
