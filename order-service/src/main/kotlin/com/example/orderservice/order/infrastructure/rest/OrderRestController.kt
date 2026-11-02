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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@Tag(name = "Orders", description = "Endpoints for managing orders and querying customers")
@RestController
@RequestMapping("/orders")
class OrderRestController(
	private val createOrder: CreateOrderUseCase,
	private val getOrder: GetOrderUseCase,
	private val getCustomerOrders: GetCustomerOrdersUseCase,
	private val getCustomers: GetCustomersUseCase,
) {
	@Operation(
		summary = "Create a new order",
		description = "Creates a new order for a customer with the specified amount and currency.",
	)
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "201",
				description = "Order created successfully",
				headers = [
					Header(
						name = HttpHeaders.LOCATION,
						description = "URI of the newly created order",
						schema = Schema(type = "string"),
					),
				],
			),
			ApiResponse(
				responseCode = "400",
				description = "Invalid order data or business validation failed",
				content = [
					Content(
						mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
						schema = Schema(implementation = ProblemDetail::class),
					),
				],
			),
		],
	)
	@PostMapping
	fun createOrder(
		@RequestBody @Valid request: CreateOrderRequest,
	): ResponseEntity<OrderResponse> {
		val response = createOrder(request.toCommand()).toResponse()
		return ResponseEntity
			.created(URI.create("/orders/${response.id}"))
			.body(response)
	}

	@Operation(
		summary = "Get order by ID",
		description = "Retrieves order details for the given unique UUID identifier.",
	)
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "Order found",
			),
			ApiResponse(
				responseCode = "404",
				description = "Order with the given ID was not found",
				content = [Content(schema = Schema(hidden = true))],
			),
			ApiResponse(
				responseCode = "400",
				description = "Invalid UUID format",
				content = [
					Content(
						mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
						schema = Schema(implementation = ProblemDetail::class),
					),
				],
			),
		],
	)
	@GetMapping("/{id}")
	fun findOrderById(
		@Parameter(
			description = "Unique order identifier (UUID)",
			example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
		)
		@PathVariable id: UUID,
	): ResponseEntity<OrderResponse> {
		val order = getOrder(GetOrderQuery(OrderId(id)))

		return ResponseEntity.ofNullable(order?.toResponse())
	}

	@Operation(
		summary = "Get customer orders",
		description = "Returns a list of all orders belonging to the specified customer.",
	)
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "Customer orders retrieved successfully",
			),
			ApiResponse(
				responseCode = "400",
				description = "Invalid request parameters (e.g. blank customerId)",
				content = [
					Content(
						mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
						schema = Schema(implementation = ProblemDetail::class),
					),
				],
			),
		],
	)
	@GetMapping
	fun findCustomerOrders(
		@ParameterObject @Valid request: GetCustomerOrdersRequest,
	): ResponseEntity<OrdersResponse> {
		val allOrders =
			getCustomerOrders(request.toQuery())
				.map(Order::toResponse)
				.let(::OrdersResponse)

		return ResponseEntity
			.ok()
			.body(allOrders)
	}

	@Operation(
		summary = "Get all customers",
		description = "Returns a list of all unique customer identifiers that have placed orders.",
	)
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "List of customers retrieved successfully",
			),
		],
	)
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
