package ru.ushakov.order.service

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import ru.ushakov.order.domain.Order

@Service
class OrderEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    fun publishOrderCreatedEvent(order: Order) {
        val event = """
            {
              "eventType": "ORDER_CREATED",
              "orderId": "${order.id}",
              "userId": "${order.userId}",
              "status": "${order.status}",
              "email": "${order.email}",
              "totalPrice": ${order.totalPrice},
              "createdAt": "${order.createdAt}"
            }
        """.trimIndent()

        kafkaTemplate.send("order-events", event)
    }

    fun publishOrderStatusUpdatedEvent(order: Order) {
        val event = """
            {
              "eventType": "ORDER_STATUS_UPDATED",
              "orderId": "${order.id}",
              "status": "${order.status}",
              "email": "${order.email}",
              "userId": "${order.userId}",
              "updatedAt": "${order.updatedAt}"
            }
        """.trimIndent()

        kafkaTemplate.send("order-events", event)
    }
}