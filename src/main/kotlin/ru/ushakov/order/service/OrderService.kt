package ru.ushakov.order.service

import com.fasterxml.jackson.databind.ObjectMapper
import ru.ushakov.order.controller.dto.CreateOrderRequest
import ru.ushakov.order.controller.dto.OrderItemResponse
import ru.ushakov.order.controller.dto.OrderResponse
import ru.ushakov.order.controller.dto.UpdateOrderStatusRequest
import ru.ushakov.order.domain.Order
import ru.ushakov.order.domain.OrderItem
import ru.ushakov.order.domain.OrderNotFoundException
import ru.ushakov.order.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import ru.ushakov.order.domain.OrderStatus
import java.time.LocalDateTime
import kotlin.random.Random

private const val ORDER_NOT_FOUND_MESSAGE = "Order not found"

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderEventPublisher: OrderEventPublisher,
    private val restTemplate: RestTemplate
) {

    val billingServiceUrl: String = System.getenv("BILLING_SERVICE_URL") ?: "http://billing-service-svc"

    val objectMapper: ObjectMapper = ObjectMapper()

    @Transactional
    fun createOrder(request: CreateOrderRequest): OrderResponse {
        val items = request.items.map { OrderItem(null, it.productId, it.quantity, calculatePrice(it.productId)) }
        val totalPrice = items.sumOf { it.quantity * it.price }
        val order = Order(userId = request.userId, items = items, totalPrice = totalPrice, email = request.email)
        orderRepository.save(order)

        orderEventPublisher.publishOrderCreatedEvent(order)

        return order.toOrderResponse()
    }

    fun payForOrder(orderId: Long, accountNumber: String) {
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE) }
        val url = "$billingServiceUrl/accounts/$accountNumber/withdraw?amount=${order.totalPrice}"

        try {
             restTemplate.put(url, null, String::class.java)
             updateStatusInternal(order, OrderStatus.PAID)
        } catch (ex: HttpServerErrorException) {
            if (ex.statusCode.value() == 500) {
                val errorResponse = objectMapper.readTree(ex.responseBodyAsString)
                val errorMessage = errorResponse?.get("message")?.asText()

                if (errorMessage == "Insufficient funds") {
                    throw RuntimeException("Insufficient funds for payment: ${order.totalPrice} р.")
                }
            }
            throw RuntimeException("Server error occurred during payment: ${ex.message}")
        } catch (ex: Exception) {
            throw RuntimeException("An error occurred during payment: ${ex.message}")
        }
    }

    fun getOrderById(orderId: Long): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE) }
        return order.toOrderResponse()
    }

    @Transactional
    fun updateOrderStatus(orderId: Long, request: UpdateOrderStatusRequest): OrderResponse {
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE) }
        val status = request.status
        updateStatusInternal(order, status)

        return order.toOrderResponse()
    }

    private fun updateStatusInternal(order: Order, status: OrderStatus) {
        order.status = status
        order.updatedAt = LocalDateTime.now()
        orderRepository.save(order)

        orderEventPublisher.publishOrderStatusUpdatedEvent(order)
    }

    @Transactional
    fun deleteOrder(orderId: Long) {
        if (!orderRepository.existsById(orderId)) {
            throw OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE)
        }
        orderRepository.deleteById(orderId)
    }

    fun getUserOrders(userId: String): List<OrderResponse> {
        val orders = orderRepository.findAllByUserId(userId)
        return orders.map { it.toOrderResponse() }
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
