package com.example.paymentservice.account.infrastructure.rest

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.CustomerId
import com.example.paymentservice.account.domain.model.of
import com.example.paymentservice.account.domain.port.incoming.DepositUseCase
import com.example.paymentservice.infrastructure.rest.GlobalRestExceptionHandler
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.mockito.kotlin.willReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.util.Currency

@WebMvcTest(AccountRestController::class)
@Import(GlobalRestExceptionHandler::class)
class AccountRestControllerTest(
	@Autowired private val mockMvc: MockMvc,
) {
	@MockitoBean
	private lateinit var depositUseCase: DepositUseCase

	private val pln = Currency.getInstance("PLN")
	private val customerId = CustomerId("customer_123")

	@Test
	fun `should deposit funds successfully and return 200 OK`() {
		// Given
		val account = Account.create(customerId, pln).deposit(BigDecimal("100.00") of pln)
		given { depositUseCase.invoke(any()) } willReturn { account }

		val requestBody =
			"""
			{
				"funds": 100.00,
				"currencyCode": "PLN"
			}
			""".trimIndent()

		// When & Then
		mockMvc
			.post("/accounts/${customerId.value}/deposits") {
				contentType = MediaType.APPLICATION_JSON
				content = requestBody
			}.andExpect {
				status { isOk() }
				content {
					jsonPath("$.id") { value(account.id.value.toString()) }
					jsonPath("$.customerId") { value(customerId.value) }
					jsonPath("$.balance") { value(100.00) }
					jsonPath("$.currencyCode") { value("PLN") }
					jsonPath("$.createdAt") { exists() }
					jsonPath("$.updatedAt") { exists() }
				}
			}

		then(depositUseCase).should().invoke(any())
	}

	@Test
	fun `should return 400 Bad Request when deposit amount is negative`() {
		// Given
		val requestBody =
			"""
			{
				"funds": -50.00,
				"currencyCode": "PLN"
			}
			""".trimIndent()

		// When & Then
		mockMvc
			.post("/accounts/${customerId.value}/deposits") {
				contentType = MediaType.APPLICATION_JSON
				content = requestBody
			}.andExpect {
				status { isBadRequest() }
				content {
					jsonPath("$.status") { value(400) }
					jsonPath("$.errors", hasSize<Any>(1))
					jsonPath("$.errors[0].field") { value("funds") }
					jsonPath("$.errors[0].rejectedValue") { value(-50.00) }
				}
			}

		then(depositUseCase).shouldHaveNoInteractions()
	}

	@Test
	fun `should return 400 Bad Request when currency code is invalid`() {
		// Given
		val requestBody =
			"""
			{
				"funds": 100.00,
				"currencyCode": "XYZ"
			}
			""".trimIndent()

		// When & Then
		mockMvc
			.post("/accounts/${customerId.value}/deposits") {
				contentType = MediaType.APPLICATION_JSON
				content = requestBody
			}.andExpect {
				status { isBadRequest() }
				content {
					jsonPath("$.status") { value(400) }
					jsonPath("$.errors", hasSize<Any>(1))
					jsonPath("$.errors[0].field") { value("currencyCode") }
					jsonPath("$.errors[0].rejectedValue") { value("XYZ") }
				}
			}

		then(depositUseCase).shouldHaveNoInteractions()
	}

	@Test
	fun `should return 400 Bad Request when customer id is blank`() {
		// Given
		val requestBody =
			"""
			{
				"funds": 100.00,
				"currencyCode": "PLN"
			}
			""".trimIndent()

		// When & Then
		mockMvc
			.post("/accounts/   /deposits") {
				contentType = MediaType.APPLICATION_JSON
				content = requestBody
			}.andExpect {
				status { isBadRequest() }
				content {
					jsonPath("$.status") { value(400) }
					jsonPath("$.errors[0].field") { value("customerId") }
				}
			}

		then(depositUseCase).shouldHaveNoInteractions()
	}

	@Test
	fun `should return 400 Bad Request when JSON is malformed`() {
		// When & Then
		mockMvc
			.post("/accounts/${customerId.value}/deposits") {
				contentType = MediaType.APPLICATION_JSON
				content = "{ invalid json }"
			}.andExpect {
				status { isBadRequest() }
				content {
					jsonPath("$.status") { value(400) }
					jsonPath("$.detail") { value("Incorrect JSON format of the request") }
				}
			}

		then(depositUseCase).shouldHaveNoInteractions()
	}
}
