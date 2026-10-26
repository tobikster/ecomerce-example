package com.example.paymentservice.account.infrastructure.rest.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class GetAccountsRequest(
	@field:NotBlank
	@field:Size(min = 1, max = 64) val customerName: String,
	val customerId: String,
)
