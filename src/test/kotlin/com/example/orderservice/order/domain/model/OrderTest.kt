package com.example.orderservice.order.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Currency

class OrderTest {
	@Test
	fun `should create order when data is valid`() {
		// Given
		val customerId = CustomerId("cust_123")
		val amount = "100.00".toBigDecimal() of Currency.getInstance("PLN")

		// When
		val order = Order.create(customerId, amount)

		// Then
		order.customerId shouldBe customerId
		order.totalAmount shouldBe amount
		order.status shouldBe OrderStatus.CREATED
	}

	@Test
	fun `should throw exception when amount is negative or zero`() {
		// Given
		val customerId = CustomerId("cust_123")
		val invalidAmount = BigDecimal("-50.00") of Currency.getInstance("PLN")

		// When & Then
		val exception =
			shouldThrow<OrderException.InvalidAmount> {
				Order.create(customerId, invalidAmount)
			}

		exception.message shouldContain "must be positive"
	}
}
