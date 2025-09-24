package com.sesac.solbid.dto.wish.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WishRequest(
        @NotNull 
        @Positive 
        Long productId
) {}