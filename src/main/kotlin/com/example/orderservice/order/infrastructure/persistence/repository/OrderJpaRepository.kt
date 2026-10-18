package com.example.orderservice.order.infrastructure.persistence.repository

import com.example.orderservice.order.infrastructure.persistence.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderJpaRepository : JpaRepository<OrderEntity, UUID>
