package com.sesac.solbid.dto.user.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NicknameAvailabilityResponse {
    private final boolean available;
}

