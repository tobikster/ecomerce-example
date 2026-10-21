package com.example.orderservice.order.infrastructure.rest

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.OrderId
import com.example.orderservice.order.domain.port.incoming.CreateOrderUseCase
import com.example.orderservice.order.domain.port.incoming.GetCustomerOrdersUseCase
import com.example.orderservice.order.domain.port.incoming.GetCustomersUseCase
import com.example.orderservice.order.domain.port.incoming.GetOrderQuery
import com.example.orderservice.order.domain.port.incoming.GetOrderUseCase
import com.example.orderservice.order.infrastructure.rest.dto.CreateOrderRequest
import com.example.orderservice.order.infrastructure.rest.dto.CustomersResponse
import com.example.orderservice.order.infrastructure.rest.dto.GetCustomerOrdersRequest
import com.example.orderservice.order.infrastructure.rest.dto.OrderResponse
import com.example.orderservice.order.infrastructure.rest.dto.OrdersResponse
import com.example.orderservice.order.infrastructure.rest.dto.toCommand
import com.example.orderservice.order.infrastructure.rest.dto.toQuery
import com.example.orderservice.order.infrastructure.rest.dto.toResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/orders")
class OrderRestController(
	private val createOrder: CreateOrderUseCase,
	private val getOrder: GetOrderUseCase,
	private val getCustomerOrders: GetCustomerOrdersUseCase,
	private val getCustomers: GetCustomersUseCase,
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

	@GetMapping("/{id}")
	fun findOrderById(
		@PathVariable id: UUID,
	): ResponseEntity<OrderResponse> {
		val order = getOrder(GetOrderQuery(OrderId(id)))

		return ResponseEntity.ofNullable(order?.toResponse())
	}

	@GetMapping
	fun findCustomerOrders(
		@Valid request: GetCustomerOrdersRequest,
	): ResponseEntity<OrdersResponse> {
		val allOrders =
			getCustomerOrders(request.toQuery())
				.map(Order::toResponse)
				.let(::OrdersResponse)

		return ResponseEntity
			.ok()
			.body(allOrders)
	}

	@GetMapping("/customers")
	fun findCustomers(): ResponseEntity<CustomersResponse> {
		val customers =
			getCustomers()
				.map(CustomerId::value)
				.let(::CustomersResponse)

		return ResponseEntity
			.ok()
			.body(customers)
	}
}
