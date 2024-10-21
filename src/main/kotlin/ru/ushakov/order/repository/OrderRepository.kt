package ru.ushakov.order.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.ushakov.order.domain.Order

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findAllByUserId(userId: String): List<Order>
}
