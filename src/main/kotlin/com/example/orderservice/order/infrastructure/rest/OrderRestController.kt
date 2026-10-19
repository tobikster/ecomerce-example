package com.example.orderservice.order.infrastructure.rest

import com.example.orderservice.order.domain.port.incoming.CreateOrderUseCase
import com.example.orderservice.order.infrastructure.rest.dto.CreateOrderRequest
import com.example.orderservice.order.infrastructure.rest.dto.OrderResponse
import com.example.orderservice.order.infrastructure.rest.dto.toCommand
import com.example.orderservice.order.infrastructure.rest.dto.toResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/orders")
class OrderRestController(
	private val createOrder: CreateOrderUseCase,
) {
	@PostMapping
	fun createOrder(
		@RequestBody @Valid request: CreateOrderRequest,
	): ResponseEntity<OrderResponse> {
		val response = createOrder(request.toCommand()).toResponse()
		return ResponseEntity
			.created(URI.create("/orders/${response.id}"))
			.body(response)
	}
}
