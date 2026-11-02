package com.example.paymentservice.common.domain.model

@JvmInline
value class CustomerId(
	val value: String,
) {
	init {
		require(value.isNotBlank()) { "Customer ID cannot be blank" }
	}
}
