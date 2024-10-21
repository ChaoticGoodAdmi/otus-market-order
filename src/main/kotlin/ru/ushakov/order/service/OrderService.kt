package ru.ushakov.order.service

import ru.ushakov.order.controller.dto.CreateOrderRequest
import ru.ushakov.order.controller.dto.OrderItemResponse
import ru.ushakov.order.controller.dto.OrderResponse
import ru.ushakov.order.controller.dto.UpdateOrderStatusRequest
import ru.ushakov.order.domain.Order
import ru.ushakov.order.domain.OrderItem
import ru.ushakov.order.domain.OrderNotFoundException
import ru.ushakov.order.repository.OrderRepository
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    @Transactional
    fun createOrder(request: CreateOrderRequest): OrderResponse {
        val items = request.items.map { OrderItem(null, it.productId, it.quantity, calculatePrice(it.productId)) }
        val totalPrice = items.sumOf { it.quantity * it.price }
        val order = Order(userId = request.userId, items = items, totalPrice = totalPrice)
        orderRepository.save(order)

        publishOrderCreatedEvent(order)

        return order.toOrderResponse()
    }

    fun getOrderById(orderId: Long): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException("Order not found") }
        return order.toOrderResponse()
    }

    @Transactional
    fun updateOrderStatus(orderId: Long, request: UpdateOrderStatusRequest): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException("Order not found") }
        order.status = request.status
        order.updatedAt = LocalDateTime.now()
        orderRepository.save(order)

        publishOrderStatusUpdatedEvent(order)

        return order.toOrderResponse()
    }

    @Transactional
    fun deleteOrder(orderId: Long) {
        if (!orderRepository.existsById(orderId)) {
            throw OrderNotFoundException("Order not found")
        }
        orderRepository.deleteById(orderId)
    }

    fun getUserOrders(userId: String): List<OrderResponse> {
        val orders = orderRepository.findAllByUserId(userId)
        return orders.map { it.toOrderResponse() }
    }

    private fun publishOrderCreatedEvent(order: Order) {
        val event = """
            {
              "eventType": "ORDER_CREATED",
              "orderId": "${order.id}",
              "userId": "${order.userId}",
              "status": "${order.status}",
              "totalPrice": ${order.totalPrice},
              "createdAt": "${order.createdAt}"
            }
        """.trimIndent()

        kafkaTemplate.send("order-events", event)
    }

    private fun publishOrderStatusUpdatedEvent(order: Order) {
        val event = """
            {
              "eventType": "ORDER_STATUS_UPDATED",
              "orderId": "${order.id}",
              "status": "${order.status}",
              "updatedAt": "${order.updatedAt}"
            }
        """.trimIndent()

        kafkaTemplate.send("order-events", event)
    }

    private fun calculatePrice(productId: String): Double {
        return Random.nextDouble(1.0, 100.0)
    }

    private fun Order.toOrderResponse(): OrderResponse {
        return OrderResponse(
            id = this.id,
            userId = this.userId,
            items = this.items.map { OrderItemResponse(it.productId, it.quantity, it.price) },
            totalPrice = this.totalPrice,
            status = this.status,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
