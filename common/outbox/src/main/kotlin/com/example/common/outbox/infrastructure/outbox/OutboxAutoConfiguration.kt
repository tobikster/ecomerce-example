package com.example.common.outbox.infrastructure.outbox

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(OutboxRepository::class, KafkaOutboxPoller::class)
class OutboxAutoConfiguration
