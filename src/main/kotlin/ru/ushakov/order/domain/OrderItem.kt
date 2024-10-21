package ru.ushakov.order.domain

import jakarta.persistence.*

@Entity
@Table(name = "order_items")
data class OrderItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val productId: String,
    val quantity: Int,
    val price: Double
)

enum class OrderStatus {
    CREATED, PAID, SHIPPED, COMPLETED, CANCELED
}