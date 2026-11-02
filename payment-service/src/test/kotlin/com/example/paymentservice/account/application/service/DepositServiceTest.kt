package com.example.paymentservice.account.application.service

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.port.incoming.DepositCommand
import com.example.paymentservice.account.domain.port.outgoing.AccountRepositoryPort
import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.of
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.willAnswer
import org.mockito.kotlin.willReturn
import java.math.BigDecimal
import java.util.Currency

class DepositServiceTest {
	private val accountRepository: AccountRepositoryPort = mock()
	private val depositService = DepositService(accountRepository)

	private val pln = Currency.getInstance("PLN")
	private val customerId = CustomerId("customer_123")

	@Test
	fun `should deposit funds into existing account and save it`() {
		// Given
		val existingAccount = Account.create(customerId, pln).deposit(BigDecimal("50.00") of pln)
		given { accountRepository.findByCustomerIdAndCurrency(customerId, pln) } willReturn { existingAccount }
		given { accountRepository.save(any()) } willAnswer { it.arguments[0] as Account }

		val command = DepositCommand(customerId, BigDecimal("100.00") of pln)

		// When
		val result = depositService(command)

		// Then
		result.balance.amount shouldBe BigDecimal("150.00")
		result.balance.currency shouldBe pln
		result.customerId shouldBe customerId
		verify(accountRepository).findByCustomerIdAndCurrency(customerId, pln)
		verify(accountRepository).save(any())
	}

	@Test
	fun `should create new account and deposit funds when account does not exist`() {
		// Given
		given { accountRepository.findByCustomerIdAndCurrency(customerId, pln) } willReturn { null }
		given { accountRepository.save(any()) } willAnswer { it.arguments[0] as Account }

		val command = DepositCommand(customerId, BigDecimal("100.00") of pln)

		// When
		val result = depositService(command)

		// Then
		result.balance.amount shouldBe BigDecimal("100.00")
		result.balance.currency shouldBe pln
		result.customerId shouldBe customerId
		verify(accountRepository).findByCustomerIdAndCurrency(customerId, pln)
		verify(accountRepository).save(any())
	}
}
