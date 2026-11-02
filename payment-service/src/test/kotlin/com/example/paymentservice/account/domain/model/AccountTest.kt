package com.example.paymentservice.account.domain.model

import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.of
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

class AccountTest {
	private val pln = Currency.getInstance("PLN")
	private val eur = Currency.getInstance("EUR")

	@Test
	fun `created account should be empty`() {
		// Given
		val customerId = CustomerId(UUID.randomUUID().toString())

		// When
		val account = Account.create(customerId, pln)

		// Then
		account.id.value.shouldNotBeNull()
		account.customerId shouldBe customerId
		account.balance.amount shouldBe BigDecimal.ZERO
		account.balance.currency shouldBe pln
		account.createdAt.shouldNotBeNull()
		account.updatedAt shouldBe account.createdAt
	}

	@Test
	fun `should throw exception when customer id is blank`() {
		// When & Then
		val emptyException =
			shouldThrow<IllegalArgumentException> {
				CustomerId("")
			}
		emptyException.message shouldContain "Customer ID cannot be blank"

		val blankException =
			shouldThrow<IllegalArgumentException> {
				CustomerId("   ")
			}
		blankException.message shouldContain "Customer ID cannot be blank"
	}

	@Test
	fun `should deposit funds into account of the same currency`() {
		// Given
		val account = Account.create(CustomerId("cust_1"), pln)
		val depositAmount = BigDecimal("150.00") of pln

		// When
		val updatedAccount = account.deposit(depositAmount)

		// Then
		updatedAccount.balance shouldBe depositAmount
		updatedAccount.updatedAt shouldBeGreaterThanOrEqualTo account.createdAt
	}

	@Test
	fun `should allow multiple consecutive deposits`() {
		// Given
		val account = Account.create(CustomerId("cust_1"), pln)

		// When
		val updatedAccount =
			account
				.deposit(BigDecimal("100.00") of pln)
				.deposit(BigDecimal("50.50") of pln)

		// Then
		updatedAccount.balance shouldBe (BigDecimal("150.50") of pln)
	}

	@Test
	fun `should throw exception when depositing funds of different currency`() {
		// Given
		val account = Account.create(CustomerId("cust_1"), pln)
		val depositInEur = BigDecimal("50.00") of eur

		// When & Then
		val exception =
			shouldThrow<IllegalArgumentException> {
				account.deposit(depositInEur)
			}

		exception.message shouldContain "Currency mismatch: cannot deposit EUR to account with PLN"
	}

	@Test
	fun `should withdraw funds when account has sufficient balance`() {
		// Given
		val account =
			Account
				.create(CustomerId("cust_1"), pln)
				.deposit(BigDecimal("200.00") of pln)

		// When
		val result = account.withdraw(BigDecimal("75.50") of pln)

		// Then
		result.shouldBeRight().should { account ->
			account.balance shouldBe (BigDecimal("124.50") of pln)
			account.updatedAt shouldBeGreaterThanOrEqualTo account.updatedAt
		}
	}

	@Test
	fun `should throw exception when withdrawing more than current balance`() {
		// Given
		val account =
			Account
				.create(CustomerId("cust_1"), pln)
				.deposit(BigDecimal("50.00") of pln)

		// When & Then
		val result = account.withdraw(BigDecimal("50.01") of pln)

		// Then
		result.shouldBeLeft().shouldBeInstanceOf<WithdrawFailure.InsufficientFunds>()
	}

	@Test
	fun `should throw exception when withdrawing funds of different currency`() {
		// Given
		val account =
			Account
				.create(CustomerId("cust_1"), pln)
				.deposit(BigDecimal("100.00") of pln)

		// When
		val result = account.withdraw(BigDecimal("10.00") of eur)

		// Then
		result.shouldBeLeft().shouldBeInstanceOf<WithdrawFailure.CurrencyMismatch>()
	}
}
