package com.sesac.solbid.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OAuth2 콜백 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class CallbackRequest {

    @NotBlank(message = "인증 코드는 필수입니다.")
    private String code;

    @NotBlank(message = "State 파라미터는 필수입니다.")
    private String state;

    @Builder
    public CallbackRequest(String code, String state) {
        this.code = code;
        this.state = state;
    }
}

