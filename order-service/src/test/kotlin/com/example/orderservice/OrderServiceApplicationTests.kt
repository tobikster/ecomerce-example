package com.example.orderservice

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.postgresql.PostgreSQLContainer

@SpringBootTest
class OrderServiceApplicationTests {
	@Test
	fun contextLoads() {
		// empty for now
	}

	companion object {
		@Container
		@ServiceConnection
		val postgres = PostgreSQLContainer("postgres:16-alpine")

		@Container
		@ServiceConnection
		val kafka = KafkaContainer("apache/kafka:4.3.2")
	}
}
