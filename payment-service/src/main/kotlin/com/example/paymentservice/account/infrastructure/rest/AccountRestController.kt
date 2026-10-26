package com.example.paymentservice.account.infrastructure.rest

import com.example.paymentservice.account.domain.port.incoming.DepositUseCase
import com.example.paymentservice.account.infrastructure.rest.dto.AccountResponse
import com.example.paymentservice.account.infrastructure.rest.dto.DepositRequest
import com.example.paymentservice.account.infrastructure.rest.dto.toCommand
import com.example.paymentservice.account.infrastructure.rest.dto.toResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
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
}
