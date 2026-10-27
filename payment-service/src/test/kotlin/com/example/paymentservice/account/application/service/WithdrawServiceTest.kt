package com.example.paymentservice.account.application.service

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.WithdrawResult
import com.example.paymentservice.account.domain.port.incoming.WithdrawCommand
import com.example.paymentservice.account.domain.port.outgoing.AccountRepositoryPort
import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.of
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.willAnswer
import org.mockito.kotlin.willReturn
import java.math.BigDecimal
import java.util.Currency

class WithdrawServiceTest {
	private val accountRepository: AccountRepositoryPort = mock()
	private val withdrawService = WithdrawService(accountRepository)

	private val pln = Currency.getInstance("PLN")
	private val customerId = CustomerId("customer_123")

	@Test
	fun `should withdraw funds from existing account and save it`() {
		// Given
		val existingAccount = Account.create(customerId, pln).deposit(BigDecimal("100.00") of pln)
		given { accountRepository.findByCustomerIdAndCurrency(customerId, pln) } willReturn { existingAccount }
		given { accountRepository.save(any()) } willAnswer { it.arguments[0] as Account }

		val command = WithdrawCommand(customerId, BigDecimal("40.00") of pln)

		// When
		val result = withdrawService(command)

		// Then
		result.shouldBeInstanceOf<WithdrawResult.Success> { (account) ->
			account.balance.amount shouldBe BigDecimal("60.00")
			account.balance.currency shouldBe pln
			account.customerId shouldBe customerId
		}
		verify(accountRepository).findByCustomerIdAndCurrency(customerId, pln)
		verify(accountRepository).save(any())
	}

	@Test
	fun `should throw InsufficientFunds when existing account has insufficient balance`() {
		// Given
		val existingAccount = Account.create(customerId, pln).deposit(BigDecimal("30.00") of pln)
		given { accountRepository.findByCustomerIdAndCurrency(customerId, pln) } willReturn { existingAccount }

		val command = WithdrawCommand(customerId, BigDecimal("50.00") of pln)

		// When
		val result = withdrawService(command)

		// Then
		result.shouldBeInstanceOf<WithdrawResult.Failure.InsufficientFunds>()
		verify(accountRepository).findByCustomerIdAndCurrency(customerId, pln)
		verify(accountRepository, never()).save(any())
	}

	@Test
	fun `should throw InsufficientFunds when account does not exist`() {
		// Given
		given { accountRepository.findByCustomerIdAndCurrency(customerId, pln) } willReturn { null }

		val command = WithdrawCommand(customerId, BigDecimal("10.00") of pln)

		// When
		val result = withdrawService(command)

		// Then
		result.shouldBeInstanceOf<WithdrawResult.Failure.InsufficientFunds>()
		verify(accountRepository).findByCustomerIdAndCurrency(customerId, pln)
		verify(accountRepository, never()).save(any())
	}
}
