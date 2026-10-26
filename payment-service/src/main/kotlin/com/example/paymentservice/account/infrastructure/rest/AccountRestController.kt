package com.example.paymentservice.account.infrastructure.rest

import com.example.paymentservice.account.domain.model.Account
import com.example.paymentservice.account.domain.model.AccountException
import com.example.paymentservice.account.domain.model.CustomerId
import com.example.paymentservice.account.domain.port.incoming.DepositUseCase
import com.example.paymentservice.account.domain.port.incoming.GetAccountsQuery
import com.example.paymentservice.account.domain.port.incoming.GetAccountsUseCase
import com.example.paymentservice.account.domain.port.incoming.WithdrawUseCase
import com.example.paymentservice.account.infrastructure.rest.dto.AccountResponse
import com.example.paymentservice.account.infrastructure.rest.dto.DepositRequest
import com.example.paymentservice.account.infrastructure.rest.dto.WithdrawRequest
import com.example.paymentservice.account.infrastructure.rest.dto.toCommand
import com.example.paymentservice.account.infrastructure.rest.dto.toResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Payment API", description = "Endpoints for management accounts and balances")
@RestController
@RequestMapping("/accounts")
class AccountRestController(
	private val deposit: DepositUseCase,
	private val withdraw: WithdrawUseCase,
	private val getAccounts: GetAccountsUseCase,
) {
	@Operation(
		summary = "Deposit funds on account",
		description =
			"Deposit funds on a account under the given currency. " +
				"If no account for the given customer and currency exists, the new one will be created.",
	)
	@PostMapping("/{customerId}/deposits")
	fun deposit(
		@PathVariable @NotBlank @Size(min = 1, max = 64) customerId: String,
		@RequestBody @Valid request: DepositRequest,
	): ResponseEntity<AccountResponse> {
		val command = request.toCommand(customerId)
		val response = deposit(command).toResponse()

		return ResponseEntity.ok(response)
	}

	@Operation(
		summary = "Withdraw funds from account",
		description =
			"Withdraw funds from an account under the given currency. " +
				"Cannot withdraw, if the account doesn't have enough funds.",
	)
	@PostMapping("/{customerId}/withdraws")
	fun withdraw(
		@PathVariable @NotBlank @Size(min = 1, max = 64) customerId: String,
		@RequestBody @Valid request: WithdrawRequest,
	): ResponseEntity<AccountResponse> {
		val command = request.toCommand(customerId)
		val response = withdraw(command).toResponse()

		return ResponseEntity.ok(response)
	}

	@GetMapping("/{customerId}")
	fun getAccounts(
		@PathVariable @NotBlank @Size(min = 1, max = 64) customerId: String,
	): ResponseEntity<List<AccountResponse>> {
		val accounts = getAccounts(GetAccountsQuery(CustomerId(customerId)))
		val response = accounts.map(Account::toResponse)

		return ResponseEntity.ok(response)
	}

	@ExceptionHandler(AccountException::class)
	fun handleAccountException(e: AccountException): ProblemDetail =
		when (e) {
			is AccountException.InsufficientFunds -> {
				ProblemDetail.forStatusAndDetail(
					HttpStatus.UNPROCESSABLE_CONTENT,
					e.message,
				)
			}
		}
}
