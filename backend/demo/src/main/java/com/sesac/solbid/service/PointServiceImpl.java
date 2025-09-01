package com.sesac.solbid.service;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.response.PointSummaryResponse;
import com.sesac.solbid.exception.PointNotFoundException;
import com.sesac.solbid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PointSummaryResponse getCurrentPoint(Long userId) {
        // 사용자 조회 (없으면 404)
        User user = userRepository.findById(userId)
                .orElseThrow(PointNotFoundException::new);

        // User.point(BigDecimal) -> long 변환 (type 일치)
        long currentPoint = user.getPoint() == null ? 0L : user.getPoint().longValue();

        // BaseEntity.updatedAt 사용 (없으면 null)
        String updatedAt = null;
        try {
            var ua = user.getUpdatedAt(); // BaseEntity에 있다고 가정
            if (ua != null) updatedAt = ua.atOffset(ZoneOffset.UTC).toString();
        } catch (NoSuchMethodError | NullPointerException ignored) {
            // BaseEntity에 updatedAt이 없거나 null이면 그냥 null 유지
        }

        return PointSummaryResponse.builder()
                .userId(user.getUserId())
                .currentPoint(currentPoint)
                .updatedAt(updatedAt)
                .build();
    }
}
