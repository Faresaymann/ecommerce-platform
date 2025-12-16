package com.example.backend.Order.Controller;


import com.example.backend.Order.dto.CreateOrderRequest;
import com.example.backend.Order.dto.DirectOrderRequest;
import com.example.backend.Order.dto.OrderResponse;
import com.example.backend.Order.dto.OrderSummaryResponse;
import com.example.backend.Order.entity.Order;
import com.example.backend.Order.exception.OrderCancellationException;
import com.example.backend.Order.exception.OrderNotFoundException;
import com.example.backend.Order.mapper.OrderMapper;
import com.example.backend.Order.service.OrderService;
import com.example.backend.auth.dto.Responses.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;



@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    /**
     * Create an order from the current user's cart.
     *
     * @param req contains the shipping address for the order
     * @param authentication Spring Security authentication object (used to get user email)
     * @return the created OrderResponse DTO with order details
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest req,
                                                     Authentication authentication) {
        String userEmail = authentication.getName();
        Order order = orderService.createOrderFromCart(userEmail, req.getShippingAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderMapper.toDto(order));
    }

    /**
     * Create a direct order for a single product without adding it to the cart.
     *
     * @param request contains productId, quantity, and shipping address
     * @param authentication Spring Security authentication object
     * @return the created OrderResponse DTO with order details
     */
    @PostMapping("/direct")
    public ResponseEntity<OrderResponse>directOrder(@Valid @RequestBody DirectOrderRequest request, Authentication authentication) {

        String userEmail = authentication.getName();
        Order order = orderService.createDirectOrder(
                userEmail,
                request.getProductId(),
                request.getQuantity(),
                request.getShippingAddress()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OrderMapper.toDto(order));
    }

    /**
     * Get a paginated list of orders for the authenticated user.
     *
     * @param pageable Spring Pageable object for page number, size, and sorting
     * @param authentication Spring Security authentication object
     * @return Page of OrderSummaryResponse DTOs
     */
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> listOrders(Pageable pageable, Authentication authentication) {
        String userEmail = authentication.getName();
        Page<Order> page = orderService.getOrdersForUser(userEmail, pageable);
        Page<OrderSummaryResponse> dtoPage = page.map(OrderMapper::toSummaryDto);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Get a single order by its ID for the authenticated user.
     *
     * @param id the Long of the order
     * @param authentication Spring Security authentication object
     * @return OrderResponse DTO of the requested order
     * @throws OrderNotFoundException if the order does not exist or does not belong to the user
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        Order order = orderService.getOrderById(id, userEmail);
        return ResponseEntity.ok(OrderMapper.toDto(order));
    }

    /**
     * Cancel an order by its ID for the authenticated user.
     *
     * Business rules:
     * - Can only cancel if order status is CREATED or PENDING_PAYMENT
     * - Cannot cancel if the order is PAID, SHIPPED, DELIVERED
     *
     * @param id the Long of the order
     * @param authentication Spring Security authentication object
     * @return 204 No Content if cancellation succeeded
     * @throws OrderNotFoundException if the order does not exist or does not belong to the user
     * @throws OrderCancellationException if the order cannot be canceled
     */

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MessageResponse> cancelOrder(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        orderService.cancelOrder(id, userEmail);
        return ResponseEntity.ok(new MessageResponse("Order cancelled."));
    }

    // keep placeholder until PaymentService exists;
    // @PostMapping("/{id}/pay")
    // public ResponseEntity<?> pay(@PathVariable Long id, @Valid @RequestBody PayRequest req,Authentication authentication) {}


}
