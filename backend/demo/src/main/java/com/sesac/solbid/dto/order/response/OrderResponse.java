package com.sesac.solbid.dto.order.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import com.sesac.solbid.domain.OrderInfo;
import com.sesac.solbid.domain.enums.DeliveryStatus;
import com.sesac.solbid.domain.enums.PaymentStatus;

public record OrderResponse(
        String id,
        String date,
        List<OrderItemResponse> items,
        BigDecimal finalPrice,
        String status,
        String statusColor,
        String trackingNumber,
        String deliveryAddress,
        Long auctionId,
        Long winnerId,
        Long sellerId,
        PaymentStatus paymentStatus,
        DeliveryStatus deliveryStatus,
        LocalDateTime orderDate) {

    public static OrderResponse from(OrderInfo orderInfo) {
        String status = getStatusText(orderInfo.getDeliveryStatus(), orderInfo.getPaymentStatus());
        String statusColor = getStatusColor(orderInfo.getDeliveryStatus(), orderInfo.getPaymentStatus());
        String formattedDate = orderInfo.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return new OrderResponse(
                orderInfo.getOrderId().toString(),
                formattedDate,
                Collections.singletonList(
                        OrderItemResponse.from(orderInfo.getAuctionEvent().getProduct(), orderInfo.getFinalPrice())),
                orderInfo.getFinalPrice(),
                status,
                statusColor,
                orderInfo.getTrackingNumber(),
                orderInfo.getDeliveryAddress(),
                orderInfo.getAuctionEvent().getAuctionEventId(),
                orderInfo.getWinner().getUserId(),
                orderInfo.getSeller().getUserId(),
                orderInfo.getPaymentStatus(),
                orderInfo.getDeliveryStatus(),
                orderInfo.getOrderDate());
    }

    private static String getStatusText(DeliveryStatus deliveryStatus, PaymentStatus paymentStatus) {
        if (paymentStatus == PaymentStatus.WAITING) {
            return "결제대기";
        }

        if (paymentStatus == PaymentStatus.FAIL) {
            return "결제실패";
        }

        return switch (deliveryStatus) {
            case PREPARING -> "배송준비중";
            case SHIPPED -> "배송중";
            case DELIVERED -> "배송완료";
            case CANCELED -> "취소·반품";
        };
    }

    private static String getStatusColor(DeliveryStatus deliveryStatus, PaymentStatus paymentStatus) {
        if (paymentStatus == PaymentStatus.WAITING) {
            return "yellow";
        }

        if (paymentStatus == PaymentStatus.FAIL) {
            return "red";
        }

        return switch (deliveryStatus) {
            case PREPARING -> "blue";
            case SHIPPED -> "purple";
            case DELIVERED -> "green";
            case CANCELED -> "red";
        };
    }
}
