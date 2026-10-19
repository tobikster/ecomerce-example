package com.example.orderservice.order.infrastructure.rest

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.OrderException
import com.example.orderservice.order.domain.model.of
import com.example.orderservice.order.domain.port.incoming.CreateOrderUseCase
import com.example.orderservice.order.infrastructure.rest.dto.CreateOrderRequest
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@WebMvcTest(OrderRestController::class)
class OrderRestControllerTest(
	@Autowired private val mockMvc: MockMvc,
) {
	@MockitoBean
	private lateinit var createOrderUseCase: CreateOrderUseCase

	@Test
	fun `should create order successfully`() {
		// Given
		val now = Instant.now()
		val order =
			Order.create(
				customerId = CustomerId("customer_123"),
				totalAmount = "150.99".toBigDecimal() of "PLN",
				now = now,
			)
		given(createOrderUseCase.invoke(any())).willReturn(order)

		val request = createOrderRequest()

		// When & then
		mockMvc
			.post("/orders") {
				contentType = MediaType.APPLICATION_JSON
				content = request
			}.andExpect {
				status { isCreated() }
				header { string(HttpHeaders.LOCATION, "/orders/${order.id}") }
				content {
					jsonPath("$.id") { value(order.id.toString()) }
					jsonPath("$.customerId") { value(order.customerId.value) }
					jsonPath("$.amount") { value(order.totalAmount.amount) }
					jsonPath("$.currencyCode") { value("PLN") }
					jsonPath("$.status") { value("created") }
					jsonPath("$.createdAt") { value(now.toString()) }
					jsonPath("$.updatedAt") { value(now.toString()) }
				}
			}
	}

	@Test
	fun `empty customer id returns bad request`() {
		// Given
		val request = createOrderRequest(customerId = "")

		// When & then
		mockMvc
			.post("/orders") {
				contentType = MediaType.APPLICATION_JSON
				content = request
			}.andExpect {
				status { isBadRequest() }
				content {
					jsonPath("$.status") { value(400) }
					jsonPath("$.detail") { value("Validation failed for one or more fields") }
					jsonPath("$.errors.customerId") { value("must not be blank") }
				}
			}
		verify(createOrderUseCase, never()).invoke(any())
	}

	@Test
	fun `domain error returns bad request`() {
		// Given
		val request = createOrderRequest(amount = "-10.00".toBigDecimal())

		given(createOrderUseCase.invoke(any()))
			.willThrow(OrderException.InvalidAmount("-10.00".toBigDecimal()))

		// When & then
		mockMvc
			.post("/orders") {
				contentType = MediaType.APPLICATION_JSON
				content = request
			}.andExpect {
				status { isBadRequest() }
				content {
					jsonPath("$.status") { value(400) }
					jsonPath("$.detail") { value("Order amount must be positive, got -10.00") }
				}
			}
	}
}

private fun createOrderRequest(
	customerId: String = "customer_123",
	amount: BigDecimal = "100.00".toBigDecimal(),
	currencyCode: String = "PLN",
) = """
	{
		"customerId": "$customerId",
		"amount": "$amount",
		"currencyCode": "$currencyCode"
	}
	""".trimIndent()
