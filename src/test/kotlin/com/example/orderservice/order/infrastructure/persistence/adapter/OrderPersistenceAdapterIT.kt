package com.example.orderservice.order.infrastructure.persistence.adapter

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.OrderId
import com.example.orderservice.order.domain.model.OrderStatus
import com.example.orderservice.order.domain.model.of
import com.example.orderservice.order.infrastructure.persistence.entity.OrderEntity
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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
import java.time.Instant
import java.util.Currency
import java.util.UUID

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(OrderPersistenceAdapter::class)
class OrderPersistenceAdapterIT
	@Autowired
	constructor(
		private val adapter: OrderPersistenceAdapter,
		private val entityManager: TestEntityManager,
	) {
		@Test
		fun `should save and retrieve order from database`() {
			// Given
			val order =
				Order.create(
					customerId = CustomerId("customer_123"),
					totalAmount = "199.99".toBigDecimal() of "PLN",
				)

			// When
			val savedOrder = adapter.save(order)

			// Then
			savedOrder.shouldNotBeNull {
				id shouldBe order.id
				totalAmount shouldBe order.totalAmount
			}

			// And
			adapter.findById(order.id) shouldNotBeNull {
				id shouldBe order.id
				customerId shouldBe order.customerId
				totalAmount shouldBe order.totalAmount
				status shouldBe OrderStatus.CREATED
			}
		}

		@Test
		fun `should find order by id when it exists`() {
			// Given
			val orderId = UUID.randomUUID()
			val entity =
				OrderEntity(
					id = orderId,
					customerId = "customer_999",
					totalAmount = "50.00".toBigDecimal(),
					currency = Currency.getInstance("PLN"),
					status = OrderStatus.CREATED,
					createdAt = Instant.now(),
					updatedAt = Instant.now(),
				)
			entityManager.persistAndFlush(entity)

			// When
			val found = adapter.findById(OrderId(orderId))

			// Then
			found.shouldNotBeNull()
			found.id.value shouldBe orderId
			found.customerId.value shouldBe "customer_999"
			found.totalAmount shouldBe ("50.00".toBigDecimal() of "PLN")
		}

		@Test
		fun `should return null when order does not exist`() {
			// Given
			val nonExistentId = OrderId(UUID.randomUUID())

			// When
			val result = adapter.findById(nonExistentId)

			// Then
			result.shouldBeNull()
		}

		companion object {
			@Container
			@ServiceConnection
			val postgres = PostgreSQLContainer("postgres:16-alpine")
		}
	}
