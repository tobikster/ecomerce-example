package com.example.orderservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class OrderServiceApplication

fun main(args: Array<String>) {
	@Suppress("SpreadOperator")
	runApplication<OrderServiceApplication>(*args)
}
