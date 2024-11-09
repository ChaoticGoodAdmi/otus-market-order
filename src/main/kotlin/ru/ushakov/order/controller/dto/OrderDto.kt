package ru.ushakov.order.controller.dto

import ru.ushakov.order.domain.OrderStatus
import java.time.LocalDateTime

data class CreateOrderRequest(
    val userId: String,
    val email: String,
    val items: List<OrderItemDTO>
)

data class OrderItemDTO(
    val productId: String,
    val quantity: Int
)

data class UpdateOrderStatusRequest(
    val status: OrderStatus
)

data class OrderResponse(
    val id: Long?,
    val userId: String,
    val items: List<OrderItemResponse>,
    val totalPrice: Double,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class OrderItemResponse(
    val productId: String,
    val quantity: Int,
    val price: Double
)

data class PaymentRequest(
    val accountNumber: String
)