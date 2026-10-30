package com.example.paymentservice.payment.infrastructure.persistence.adapter

import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.OrderId
import com.example.paymentservice.common.domain.model.of
import com.example.paymentservice.payment.domain.model.Payment
import com.example.paymentservice.payment.domain.model.PaymentStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(PaymentPersistenceAdapter::class)
class PaymentPersistenceAdapterIT(
	@Autowired private val adapter: PaymentPersistenceAdapter,
	@Autowired private val entityManager: TestEntityManager,
) {
	private val pln = Currency.getInstance("PLN")

	@Test
	fun `should save and find completed payment by order id`() {
		// Given
		val orderId = OrderId(UUID.randomUUID())
		val customerId = CustomerId("cust_123")
		val amount = BigDecimal("150.00") of pln
		val payment = Payment.completed(orderId, customerId, amount)

		// When
		val saved = adapter.save(payment)

		// Then
		saved.shouldNotBeNull()
		saved.id shouldBe payment.id
		saved.orderId shouldBe orderId
		saved.customerId shouldBe customerId
		saved.amount.amount.shouldBeEqualComparingTo(BigDecimal("150.00"))
		saved.amount.currency shouldBe pln
		saved.status shouldBe PaymentStatus.COMPLETED
		saved.failureReason.shouldBeNull()

		// And
		val retrieved = adapter.findByOrderId(orderId)
		retrieved.shouldNotBeNull()
		retrieved.id shouldBe payment.id
		retrieved.orderId shouldBe orderId
		retrieved.customerId shouldBe customerId
		retrieved.amount.amount.shouldBeEqualComparingTo(BigDecimal("150.00"))
		retrieved.amount.currency shouldBe pln
		retrieved.status shouldBe PaymentStatus.COMPLETED
		retrieved.failureReason.shouldBeNull()
	}

	@Test
	fun `should save and find failed payment with failure reason`() {
		// Given
		val orderId = OrderId(UUID.randomUUID())
		val customerId = CustomerId("cust_456")
		val amount = BigDecimal("75.50") of pln
		val payment = Payment.failed(orderId, customerId, amount, failureReason = "Insufficient funds")

		// When
		val saved = adapter.save(payment)

		// Then
		saved.shouldNotBeNull()
		saved.status shouldBe PaymentStatus.FAILED
		saved.failureReason shouldBe "Insufficient funds"

		// And
		val retrieved = adapter.findByOrderId(orderId)
		retrieved.shouldNotBeNull()
		retrieved.id shouldBe payment.id
		retrieved.orderId shouldBe orderId
		retrieved.customerId shouldBe customerId
		retrieved.amount.amount.shouldBeEqualComparingTo(BigDecimal("75.50"))
		retrieved.status shouldBe PaymentStatus.FAILED
		retrieved.failureReason shouldBe "Insufficient funds"
	}

	@Test
	fun `should return null when payment for order id does not exist`() {
		// Given
		val nonExistentOrderId = OrderId(UUID.randomUUID())

		// When
		val result = adapter.findByOrderId(nonExistentOrderId)

		// Then
		result.shouldBeNull()
	}

	@Test
	fun `should fail when saving payment with duplicate order id`() {
		// Given
		val orderId = OrderId(UUID.randomUUID())
		val payment1 = Payment.completed(orderId, CustomerId("cust_1"), BigDecimal("50.00") of pln)
		val payment2 = Payment.failed(orderId, CustomerId("cust_2"), BigDecimal("100.00") of pln, failureReason = "Duplicate")

		adapter.save(payment1)
		entityManager.flush()

		// When & Then
		shouldThrow<ConstraintViolationException> {
			adapter.save(payment2)
			entityManager.flush()
		}
	}

	companion object {
		@Container
		@ServiceConnection
		val postgres = PostgreSQLContainer("postgres:16-alpine")
	}
}
