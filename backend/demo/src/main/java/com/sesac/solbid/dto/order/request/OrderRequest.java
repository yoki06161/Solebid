package com.sesac.solbid.dto.order.request;

import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotNull Long auctionId,
        @NotNull String deliveryAddress
) {}
