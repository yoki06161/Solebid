package com.sesac.solbid.service.order;

import com.sesac.solbid.domain.AuctionEvent;
import com.sesac.solbid.domain.OrderInfo;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.order.request.OrderRequest;
import com.sesac.solbid.dto.order.response.OrderResponse;
import com.sesac.solbid.repository.AuctionEventRepository;
import com.sesac.solbid.repository.UserRepository;
import com.sesac.solbid.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AuctionEventRepository auctionEventRepository;
    private final UserRepository userRepository;

    /**
    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long winnerId) {
        AuctionEvent auctionEvent = auctionEventRepository
                .findById(request.auctionId())
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        User winner = userRepository
                .findById(winnerId)
                .orElseThrow(() -> new IllegalArgumentException("Winner not found"));

        OrderInfo newOrder = new OrderInfo(
                auctionEvent,
                winner,
                auctionEvent.getSeller(),
                auctionEvent.getHighestBidAmount(),
                request.deliveryAddress());

        OrderInfo savedOrder = orderRepository.save(newOrder);
        return OrderResponse.from(savedOrder);
    }*/

    @Override
    public OrderResponse findOrder(Long orderId) {
        OrderInfo orderInfo = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return OrderResponse.from(orderInfo);
    }

    @Override
    public List<OrderResponse> findOrdersByWinner(Long winnerId) {
        User winner = userRepository
                .findById(winnerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return orderRepository
                .findByWinner(winner)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }
}
