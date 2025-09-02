package com.sesac.solbid.dto.request;

import com.sesac.solbid.domain.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentRecordSearchRequest {
    @NotNull(message = "userId는 필수입니다.")
    private Long userId;

    private PaymentStatus status; // SUCCESS/FAIL/...

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    // 전환건만 보기: charged=true OR converted_point>0
    private Boolean convertedOnly = true;
}