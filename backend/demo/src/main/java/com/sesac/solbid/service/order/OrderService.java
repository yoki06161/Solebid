package com.sesac.solbid.service.order;

import java.util.List;

import com.sesac.solbid.dto.order.request.OrderRequest;
import com.sesac.solbid.dto.order.response.OrderResponse;

public interface OrderService {

        OrderResponse createOrder(OrderRequest request, Long winnerId);

        OrderResponse findOrder(Long orderId);

        List<OrderResponse> findOrdersByWinner(Long winnerId);
}