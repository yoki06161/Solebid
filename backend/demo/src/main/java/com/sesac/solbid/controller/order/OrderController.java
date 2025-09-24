package com.sesac.solbid.controller.order;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.order.request.OrderRequest;
import com.sesac.solbid.dto.order.response.OrderResponse;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    /**
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody OrderRequest request,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        OrderResponse createdOrder = orderService.createOrder(request, user.getUserId());
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }*/

    @GetMapping("/winnings")
    public ResponseEntity<List<OrderResponse>> getWinningOrders(@AuthenticationPrincipal User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        List<OrderResponse> orders = orderService.findOrdersByWinner(user.getUserId());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable Long orderId) {
        OrderResponse order = orderService.findOrder(orderId);
        return ResponseEntity.ok(order);
    }
}
