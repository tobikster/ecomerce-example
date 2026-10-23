package com.example.orderservice.order.infrastructure.rest

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.OrderException
import com.example.orderservice.order.domain.model.OrderId
import com.example.orderservice.order.domain.model.OrderStatus
import com.example.orderservice.order.domain.model.of
import com.example.orderservice.order.domain.port.incoming.CreateOrderUseCase
import com.example.orderservice.order.domain.port.incoming.GetCustomerOrdersQuery
import com.example.orderservice.order.domain.port.incoming.GetCustomerOrdersUseCase
import com.example.orderservice.order.domain.port.incoming.GetCustomersUseCase
import com.example.orderservice.order.domain.port.incoming.GetOrderQuery
import com.example.orderservice.order.domain.port.incoming.GetOrderUseCase
import com.example.orderservice.order.infrastructure.rest.dto.OrderResponse
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
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

	@MockitoBean
	private lateinit var getOrderUseCase: GetOrderUseCase

	@MockitoBean
	private lateinit var getCustomerOrdersUseCase: GetCustomerOrdersUseCase

	@MockitoBean
	private lateinit var getCustomersUseCase: GetCustomersUseCase

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

	@Test
	fun `should return order by id when order exists`() {
		// Given
		val orderId = OrderId(UUID.randomUUID())
		val order =
			Order.restore(
				id = orderId,
				customerId = CustomerId("customer_123"),
				totalAmount = "150.99".toBigDecimal() of "PLN",
				status = OrderStatus.CREATED,
				createdAt = Instant.now(),
				updatedAt = Instant.now(),
			)
		given(getOrderUseCase.invoke(GetOrderQuery(orderId))).willReturn(order)

		// When & then
		mockMvc
			.get("/orders/${orderId.value}")
			.andExpect {
				status { isOk() }
				content {
					jsonPath("$.id") { value(orderId.value.toString()) }
					jsonPath("$.customerId") { value("customer_123") }
					jsonPath("$.amount") { value(150.99) }
					jsonPath("$.currencyCode") { value("PLN") }
					jsonPath("$.status") { value("created") }
				}
			}
	}

	@Test
	fun `should return 404 when order by id does not exist`() {
		// Given
		val nonExistentId = UUID.randomUUID()
		given(getOrderUseCase.invoke(GetOrderQuery(OrderId(nonExistentId)))).willReturn(null)

		// When & then
		mockMvc
			.get("/orders/$nonExistentId")
			.andExpect {
				status { isNotFound() }
			}
	}

	@Test
	fun `should return 400 when order id is not a valid uuid`() {
		// When & then
		mockMvc
			.get("/orders/invalid-uuid")
			.andExpect {
				status { isBadRequest() }
			}
		then(getOrderUseCase).shouldHaveNoInteractions()
	}

	@Test
	fun `successfully return all customer's orders`() {
		// Given
		val orders =
			listOf(
				Order.restore(
					id = OrderId(UUID.randomUUID()),
					customerId = CustomerId("customer_123"),
					totalAmount = "100.00".toBigDecimal() of "PLN",
					status = OrderStatus.CREATED,
					createdAt = Instant.now(),
					updatedAt = Instant.now(),
				),
				Order.restore(
					id = OrderId(UUID.randomUUID()),
					customerId = CustomerId("customer_123"),
					totalAmount = "75.50".toBigDecimal() of "EUR",
					status = OrderStatus.PAID,
					createdAt = Instant.now(),
					updatedAt = Instant.now(),
				),
			)
		val expectedQuery = GetCustomerOrdersQuery(CustomerId("customer_123"))
		given(getCustomerOrdersUseCase.invoke(expectedQuery)).willReturn(orders)

		// When & then
		mockMvc
			.get("/orders?customerId=customer_123")
			.andExpect {
				status { isOk() }
				content {
					jsonPath("$.orders") { value(hasSize<OrderResponse>(2)) }
					jsonPath("$.orders[0].customerId") { value("customer_123") }
					jsonPath("$.orders[0].status") { value("created") }
				}
			}
	}

	@Test
	fun `successfully return empty orders`() {
		// Given
		given(getCustomerOrdersUseCase.invoke(any())).willReturn(emptyList())

		// When & then
		mockMvc
			.get("/orders?customerId=customer_123")
			.andExpect {
				status { isOk() }
				content {
					jsonPath("$.orders") { isEmpty() }
				}
			}
	}

	@Test
	fun `when get orders query is incorrect, then response should be 400`() {
		// When & then
		mockMvc
			.get("/orders")
			.andExpect { status { isBadRequest() } }
		then(getCustomerOrdersUseCase).shouldHaveNoInteractions()
	}

	@Test
	fun `when get customers is called, then response should be 200`() {
		// Given
		given(getCustomersUseCase.invoke())
			.willReturn(listOf(CustomerId("customer_123"), CustomerId("customer_456")))

		// When & then
		mockMvc
			.get("/orders/customers")
			.andExpect {
				status { isOk() }
				content {
					jsonPath("$.customers") {
						value(hasSize<String>(2))
						value(containsInAnyOrder("customer_123", "customer_456"))
					}
				}
			}
	}

	@Test
	fun `when there is no customers, then empty list is returned`() {
		// Given
		given(getCustomersUseCase.invoke()).willReturn(emptyList())

		// When & then
		mockMvc
			.get("/orders/customers")
			.andExpect {
				status { isOk() }
				content { jsonPath("$.customers") { isEmpty() } }
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
