package com.example.paymentservice.account.infrastructure.persistence.adapter

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.CustomerId
import com.example.paymentservice.account.domain.model.of
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.math.BigDecimal
import java.util.Currency

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(AccountPersistenceAdapter::class)
class AccountPersistenceAdapterIT(
	@Autowired private val adapter: AccountPersistenceAdapter,
) {
	private val pln = Currency.getInstance("PLN")
	private val eur = Currency.getInstance("EUR")

	@Test
	fun `should save and find account by customer id and currency`() {
		// Given
		val customerId = CustomerId("cust_123")
		val account =
			Account
				.create(customerId, pln)
				.deposit(BigDecimal("150.00") of pln)

		// When
		val saved = adapter.save(account)

		// Then
		saved.shouldNotBeNull()
		saved.id shouldBe account.id
		saved.balance.amount.shouldBeEqualComparingTo(BigDecimal("150.00"))
		saved.balance.currency shouldBe pln

		// And
		val retrieved = adapter.findByCustomerIdAndCurrency(customerId, pln)
		retrieved.shouldNotBeNull()
		retrieved.id shouldBe account.id
		retrieved.customerId shouldBe customerId
		retrieved.balance.amount.shouldBeEqualComparingTo(BigDecimal("150.00"))
		retrieved.balance.currency shouldBe pln
	}

	@Test
	fun `should update balance of existing account`() {
		// Given
		val customerId = CustomerId("cust_456")
		val initialAccount =
			Account
				.create(customerId, pln)
				.deposit(BigDecimal("100.00") of pln)
		adapter.save(initialAccount)

		// When
		val updatedAccount = initialAccount.deposit(BigDecimal("50.25") of pln)
		adapter.save(updatedAccount)

		// Then
		val retrieved = adapter.findByCustomerIdAndCurrency(customerId, pln)
		retrieved.shouldNotBeNull()
		retrieved.balance.amount.shouldBeEqualComparingTo(BigDecimal("150.25"))
	}

	@Test
	fun `should return null when account does not exist`() {
		// Given
		val nonExistentCustomer = CustomerId("non_existent")

		// When
		val result = adapter.findByCustomerIdAndCurrency(nonExistentCustomer, pln)

		// Then
		result.shouldBeNull()
	}

	@Test
	fun `should handle multiple accounts with different currencies for the same customer`() {
		// Given
		val customerId = CustomerId("multi_currency_cust")
		val plnAccount =
			Account
				.create(customerId, pln)
				.deposit(BigDecimal("200.00") of pln)
		val eurAccount =
			Account
				.create(customerId, eur)
				.deposit(BigDecimal("50.00") of eur)

		// When
		adapter.save(plnAccount)
		adapter.save(eurAccount)

		// Then
		val retrievedPln = adapter.findByCustomerIdAndCurrency(customerId, pln)
		retrievedPln.shouldNotBeNull()
		retrievedPln.balance.amount.shouldBeEqualComparingTo(BigDecimal("200.00"))
		retrievedPln.balance.currency shouldBe pln

		val retrievedEur = adapter.findByCustomerIdAndCurrency(customerId, eur)
		retrievedEur.shouldNotBeNull()
		retrievedEur.balance.amount.shouldBeEqualComparingTo(BigDecimal("50.00"))
		retrievedEur.balance.currency shouldBe eur
	}

	companion object {
		@Container
		@ServiceConnection
		val postgres = PostgreSQLContainer("postgres:16-alpine")
	}
}
