package com.example.paymentservice.account.infrastructure.rest

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.AccountException
import com.example.paymentservice.account.domain.model.AccountId
import com.example.paymentservice.account.domain.model.CustomerId
import com.example.paymentservice.account.domain.model.of
import com.example.paymentservice.account.domain.port.incoming.DepositUseCase
import com.example.paymentservice.account.domain.port.incoming.WithdrawUseCase
import com.example.paymentservice.infrastructure.rest.GlobalRestExceptionHandler
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.mockito.kotlin.willReturn
import org.mockito.kotlin.willThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

@WebMvcTest(AccountRestController::class)
@Import(GlobalRestExceptionHandler::class)
class AccountRestControllerTest(
	@Autowired private val mockMvc: MockMvc,
) {
	@MockitoBean
	private lateinit var depositUseCase: DepositUseCase

	@MockitoBean
	private lateinit var withdrawUseCase: WithdrawUseCase

	private val pln = Currency.getInstance("PLN")
	private val customerId = CustomerId("customer_123")

	@Test
	fun `should deposit funds successfully and return 200 OK`() {
		// Given
		val account = Account.create(customerId, pln).deposit(BigDecimal("100.00") of pln)
		given { depositUseCase.invoke(any()) } willReturn { account }

		val requestBody = createDepositRequest()

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
		val requestBody = createDepositRequest(funds = BigDecimal("-50.00"))

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
	fun `should return 400 Bad Request when deposit currency code is invalid`() {
		// Given
		val requestBody = createDepositRequest(currencyCode = "XYZ")

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
	fun `should return 400 Bad Request when customer id is blank during deposit`() {
		// Given
		val requestBody = createDepositRequest()

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

	@Test
	fun `should withdraw funds successfully and return 200 OK`() {
		// Given
		val account =
			Account.restore(
				id = AccountId(UUID.randomUUID()),
				customerId = customerId,
				balance = BigDecimal("100.00") of pln,
			)
		given { withdrawUseCase.invoke(any()) } willReturn { account }

		val requestBody = createWithdrawRequest()

		// When & Then
		mockMvc
			.post("/accounts/${customerId.value}/withdraws") {
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

		then(withdrawUseCase).should().invoke(any())
	}

	@Test
	fun `should return 400 Bad Request when withdraw amount is negative`() {
		// Given
		val requestBody = createWithdrawRequest(amount = BigDecimal("-50.00"))

		// When & Then
		mockMvc
			.post("/accounts/${customerId.value}/withdraws") {
				contentType = MediaType.APPLICATION_JSON
				content = requestBody
			}.andExpect {
				status { isBadRequest() }
				content {
					jsonPath("$.status") { value(400) }
					jsonPath("$.errors", hasSize<Any>(1))
					jsonPath("$.errors[0].field") { value("amount") }
					jsonPath("$.errors[0].rejectedValue") { value(-50.00) }
				}
			}

		then(withdrawUseCase).shouldHaveNoInteractions()
	}

	@Test
	fun `should return 400 Bad Request when withdraw currency code is invalid`() {
		// Given
		val requestBody = createWithdrawRequest(currencyCode = "XYZ")

		// When & Then
		mockMvc
			.post("/accounts/${customerId.value}/withdraws") {
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

		then(withdrawUseCase).shouldHaveNoInteractions()
	}

	@Test
	fun `should return 400 Bad Request when customer id is blank during withdraw`() {
		// Given
		val requestBody = createWithdrawRequest()

		// When & Then
		mockMvc
			.post("/accounts/   /withdraws") {
				contentType = MediaType.APPLICATION_JSON
				content = requestBody
			}.andExpect {
				status { isBadRequest() }
				content {
					jsonPath("$.status") { value(400) }
					jsonPath("$.errors[0].field") { value("customerId") }
				}
			}

		then(withdrawUseCase).shouldHaveNoInteractions()
	}

	@Test
	fun `should return 422 Unprocessable Content when account has insufficient funds`() {
		// Given
		val insufficientFundsException =
			AccountException.InsufficientFunds(
				requested = BigDecimal("150.00") of pln,
				available = BigDecimal("50.00") of pln,
			)
		given { withdrawUseCase.invoke(any()) } willThrow { insufficientFundsException }

		val requestBody = createWithdrawRequest(amount = BigDecimal("150.00"))

		// When & Then
		mockMvc
			.post("/accounts/${customerId.value}/withdraws") {
				contentType = MediaType.APPLICATION_JSON
				content = requestBody
			}.andExpect {
				status { isUnprocessableContent() }
				content {
					jsonPath("$.status") { value(422) }
					jsonPath("$.detail") { value(containsString("Insufficient funds")) }
				}
			}

		then(withdrawUseCase).should().invoke(any())
	}

	private fun createDepositRequest(
		funds: BigDecimal = BigDecimal("100.00"),
		currencyCode: String = "PLN",
	) = """
		{
			"funds": $funds,
			"currencyCode": "$currencyCode"
		}
		""".trimIndent()

	private fun createWithdrawRequest(
		amount: BigDecimal = BigDecimal("100.00"),
		currencyCode: String = "PLN",
	) = """
		{
			"amount": $amount,
			"currencyCode": "$currencyCode"
		}
		""".trimIndent()
}
