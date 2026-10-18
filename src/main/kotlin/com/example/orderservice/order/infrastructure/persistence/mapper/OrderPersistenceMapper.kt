package com.example.orderservice.order.infrastructure.persistence.mapper

import com.example.orderservice.order.domain.model.CustomerId
import com.example.orderservice.order.domain.model.Money
import com.example.orderservice.order.domain.model.Order
import com.example.orderservice.order.domain.model.OrderId
import com.example.orderservice.order.infrastructure.persistence.entity.OrderEntity

fun Order.toEntity() =
	OrderEntity(
		id = id.value,
		customerId = customerId.value,
		totalAmount = totalAmount.amount,
		currency = totalAmount.currency,
		status = status,
		createdAt = createdAt,
		updatedAt = updatedAt,
	)

fun OrderEntity.toDomain() =
	Order.restore(
		id = OrderId(id),
		customerId = CustomerId(customerId),
		totalAmount = Money(totalAmount, currency),
		status = status,
		createdAt = createdAt,
		updatedAt = updatedAt,
	)
