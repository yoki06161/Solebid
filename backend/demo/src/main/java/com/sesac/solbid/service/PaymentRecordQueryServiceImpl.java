package com.sesac.solbid.service;

import com.sesac.solbid.dto.request.PaymentRecordSearchRequest;
import com.sesac.solbid.dto.response.PageResponse;
import com.sesac.solbid.dto.response.PaymentRecordItemResponse;
import com.sesac.solbid.repository.PaymentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
class PaymentRecordQueryServiceImpl implements PaymentRecordQueryService {

    private final PaymentsRepository paymentsRepository;

    // 허용 정렬 필드 (화이트리스트)
    private static final Set<String> ALLOWED_SORT = Set.of("requestedAt", "amount", "paymentStatus");

    @Override
    public PageResponse<PaymentRecordItemResponse> getRecords(PaymentRecordSearchRequest req, Pageable pageable) {
        Assert.notNull(req.getUserId(), "userId is required");

        // convertedOnly 기본값
        boolean convertedOnly = req.getConvertedOnly() == null || req.getConvertedOnly();

        // 날짜 변환 (LocalDate → 하루 시작/끝)
        LocalDateTime fromAt = toStart(req.getFrom());
        LocalDateTime toAt   = toEnd(req.getTo());

        //정렬 화이트리스트 적용
        Pageable safePageable = sanitizeSort(pageable);

        log.debug("[PAYMENTS][QUERY] userId={}, status={}, from={}, to={}, convertedOnly={}, pageable={}",
                req.getUserId(), req.getStatus(), fromAt, toAt, convertedOnly, safePageable);

        var page = paymentsRepository.findRecordsByUserId(
                req.getUserId(), req.getStatus(), fromAt, toAt, convertedOnly, safePageable);

        return new PageResponse<>(page);
    }

    private LocalDateTime toStart(LocalDate d) { return d == null ? null : d.atStartOfDay(); }
    private LocalDateTime toEnd(LocalDate d)   { return d == null ? null : d.atTime(LocalTime.MAX); }

    private Pageable sanitizeSort(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable == null ? 0 : pageable.getPageNumber(),
                    pageable == null ? 20 : pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "requestedAt")
            );
        }
        // 허용 필드만 유지
        Sort filtered = Sort.by(
                pageable.getSort().stream()
                        .filter(o -> ALLOWED_SORT.contains(o.getProperty()))
                        .toList()
        );
        if (filtered.isUnsorted()) {
            filtered = Sort.by(Sort.Direction.DESC, "requestedAt");
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), filtered);
    }
}