package com.example.paymentservice.account.infrastructure.rest

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.AccountId
import com.example.paymentservice.account.domain.model.WithdrawResult
import com.example.paymentservice.account.domain.port.incoming.DepositUseCase
import com.example.paymentservice.account.domain.port.incoming.GetAccountsQuery
import com.example.paymentservice.account.domain.port.incoming.GetAccountsUseCase
import com.example.paymentservice.account.domain.port.incoming.WithdrawUseCase
import com.example.paymentservice.common.domain.model.CustomerId
import com.example.paymentservice.common.domain.model.of
import com.example.paymentservice.infrastructure.rest.GlobalRestExceptionHandler
import org.hamcrest.Matchers.containsString
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
import org.springframework.test.web.servlet.get
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

	@MockitoBean
	private lateinit var getAccountsUseCase: GetAccountsUseCase

	private val pln = Currency.getInstance("PLN")
	private val eur = Currency.getInstance("EUR")
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
		given { withdrawUseCase.invoke(any()) } willReturn { WithdrawResult.Success(account) }

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
		given { withdrawUseCase.invoke(any()) } willReturn {
			WithdrawResult.Failure.InsufficientFunds(
				BigDecimal("150.00") of pln,
				BigDecimal("50.00") of pln,
			)
		}

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

	@Test
	fun `should return customer accounts and 200 OK when customer has accounts`() {
		// Given
		val accountPln =
			Account.restore(
				id = AccountId(UUID.randomUUID()),
				customerId = customerId,
				balance = BigDecimal("100.00") of pln,
			)
		val accountEur =
			Account.restore(
				id = AccountId(UUID.randomUUID()),
				customerId = customerId,
				balance = BigDecimal("50.00") of eur,
			)
		given { getAccountsUseCase.invoke(GetAccountsQuery(customerId)) } willReturn { listOf(accountPln, accountEur) }

		// When & Then
		mockMvc
			.get("/accounts/${customerId.value}")
			.andExpect {
				status { isOk() }
				content {
					jsonPath("$", hasSize<Any>(2))
					jsonPath("$[0].id") { value(accountPln.id.value.toString()) }
					jsonPath("$[0].customerId") { value(customerId.value) }
					jsonPath("$[0].balance") { value(100.00) }
					jsonPath("$[0].currencyCode") { value("PLN") }
					jsonPath("$[1].id") { value(accountEur.id.value.toString()) }
					jsonPath("$[1].customerId") { value(customerId.value) }
					jsonPath("$[1].balance") { value(50.00) }
					jsonPath("$[1].currencyCode") { value("EUR") }
				}
			}

		then(getAccountsUseCase).should().invoke(GetAccountsQuery(customerId))
	}

	@Test
	fun `should return empty list and 200 OK when customer has no accounts`() {
		// Given
		given { getAccountsUseCase.invoke(GetAccountsQuery(customerId)) } willReturn { emptyList() }

		// When & Then
		mockMvc
			.get("/accounts/${customerId.value}")
			.andExpect {
				status { isOk() }
				content {
					jsonPath("$", hasSize<Any>(0))
				}
			}

		then(getAccountsUseCase).should().invoke(GetAccountsQuery(customerId))
	}

	@Test
	fun `should return 400 Bad Request when customer id is blank during get accounts`() {
		// When & Then
		mockMvc
			.get("/accounts/   ")
			.andExpect {
				status { isBadRequest() }
				content {
					jsonPath("$.status") { value(400) }
					jsonPath("$.errors[0].field") { value("customerId") }
				}
			}

		then(getAccountsUseCase).shouldHaveNoInteractions()
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
