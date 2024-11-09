package ru.ushakov.order.domain

import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "orders")
data class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val userId: String,

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val items: List<OrderItem>,

    val totalPrice: Double = 0.0,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.CREATED,
    var email: String,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)