package com.example.paymentservice.account.domain.model

import com.example.paymentservice.common.domain.model.Money
import com.example.paymentservice.common.domain.model.of
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Currency

class MoneyTest {
	private val pln = Currency.getInstance("PLN")
	private val eur = Currency.getInstance("EUR")

	@Test
	fun `should create money when amount is zero or positive`() {
		// Given & When
		val zeroMoney = Money(BigDecimal.ZERO, pln)
		val positiveMoney = Money(BigDecimal("100.50"), pln)

		// Then
		zeroMoney.amount shouldBe BigDecimal.ZERO
		zeroMoney.currency shouldBe pln
		positiveMoney.amount shouldBe BigDecimal("100.50")
		positiveMoney.currency shouldBe pln
	}

	@Test
	fun `should throw exception when creating money with negative amount`() {
		// When & Then
		val exception =
			shouldThrow<IllegalArgumentException> {
				Money(BigDecimal("-0.01"), pln)
			}

		exception.message shouldContain "Amount cannot be negative"
	}

	@Test
	fun `should correctly add money of the same currency`() {
		// Given
		val m1 = BigDecimal("50.25") of pln
		val m2 = BigDecimal("49.75") of pln

		// When
		val result = m1 + m2

		// Then
		result shouldBe (BigDecimal("100.00") of pln)
	}

	@Test
	fun `should throw exception when adding money of different currencies`() {
		// Given
		val m1 = BigDecimal("50.00") of pln
		val m2 = BigDecimal("50.00") of eur

		// When & Then
		val exception =
			shouldThrow<IllegalArgumentException> {
				m1 + m2
			}

		exception.message shouldContain "Currency mismatch: cannot add EUR to PLN"
	}

	@Test
	fun `should correctly subtract money of the same currency`() {
		// Given
		val m1 = BigDecimal("100.00") of pln
		val m2 = BigDecimal("40.00") of pln

		// When
		val result = m1 - m2

		// Then
		result shouldBe (BigDecimal("60.00") of pln)
	}

	@Test
	fun `should throw exception when subtracting results in negative amount`() {
		// Given
		val m1 = BigDecimal("50.00") of pln
		val m2 = BigDecimal("50.01") of pln

		// When & Then
		val exception =
			shouldThrow<IllegalArgumentException> {
				m1 - m2
			}

		exception.message shouldContain "Amount cannot be negative"
	}

	@Test
	fun `should throw exception when subtracting money of different currencies`() {
		// Given
		val m1 = BigDecimal("100.00") of pln
		val m2 = BigDecimal("50.00") of eur

		// When & Then
		val exception =
			shouldThrow<IllegalArgumentException> {
				m1 - m2
			}

		exception.message shouldContain "Currency mismatch: cannot subtract EUR from PLN"
	}

	@Test
	fun `should create zero money for given currency`() {
		// When
		val zeroPln = Money.zero(pln)

		// Then
		zeroPln.amount shouldBe BigDecimal.ZERO
		zeroPln.currency shouldBe pln
	}

	@Test
	fun `should support infix of with Currency and String currencyCode`() {
		// When
		val fromCurrency = BigDecimal("12.34") of pln
		val fromString = BigDecimal("12.34") of "PLN"

		// Then
		fromCurrency shouldBe fromString
		fromString.currency shouldBe pln
	}
}
