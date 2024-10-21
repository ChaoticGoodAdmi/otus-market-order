package ru.ushakov.order.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.ushakov.order.controller.dto.CreateOrderRequest
import ru.ushakov.order.controller.dto.OrderResponse
import ru.ushakov.order.controller.dto.UpdateOrderStatusRequest
import ru.ushakov.order.service.OrderService

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody request: CreateOrderRequest): OrderResponse {
        return orderService.createOrder(request)
    }

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: Long): OrderResponse {
        return orderService.getOrderById(orderId)
    }

    @PatchMapping("/{orderId}/status")
    fun updateOrderStatus(@PathVariable orderId: Long, @RequestBody request: UpdateOrderStatusRequest): OrderResponse {
        return orderService.updateOrderStatus(orderId, request)
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteOrder(@PathVariable orderId: Long) {
        orderService.deleteOrder(orderId)
    }

    @GetMapping("/users/{userId}")
    fun getUserOrders(@PathVariable userId: String): List<OrderResponse> {
        return orderService.getUserOrders(userId)
    }
}