package com.sesac.solbid.service.point;

import com.sesac.solbid.domain.PointTransaction;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.TransEnum;
import com.sesac.solbid.dto.payment.response.PointSummaryResponse;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.repository.payment.PointTransactionRepository;
import com.sesac.solbid.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final UserRepository userRepository;
    private final PointTransactionRepository txRepo;


    /**유저 포인트 단건 조회*/
    @Override
    @Transactional(readOnly = true)
    public PointSummaryResponse getCurrentPoint(Long userId) {
        // 사용자 조회 (없으면 404)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // User.point(BigDecimal) -> long 변환 (type 일치)
        long currentPoint = user.getPoint() == null ? 0L : user.getPoint().longValue();

        String updatedAt = null;
        try {
            var ua = user.getUpdatedAt(); // BaseEntity에 있다고 가정
            if (ua != null) updatedAt = ua.atOffset(ZoneOffset.UTC).toString();
        } catch (NoSuchMethodError | NullPointerException ignored) {
        }

        return PointSummaryResponse.builder()
                .userId(user.getUserId())
                .currentPoint(currentPoint)
                .updatedAt(updatedAt)
                .build();
    }

    /** 낙찰 즉시 차감 */
    @Transactional
    @Override
    public void capture(Long userId, BigDecimal amount, Long auctionEventId, String description) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 도메인 메서드로 차감(잔액 검증 포함)
        user.debitPoint(amount);
        userRepository.save(user);

        // 트랜잭션 기록 (차감은 음수)
        txRepo.save(PointTransaction.builder()
                .user(user)
                .transEnum(TransEnum.CAPTURE)
                .amount(amount.negate())
                .balanceAfter(user.getPoint())
                .description(description)
                .auctionEventId(auctionEventId)
                .build());
    }
    /** 결제(차감) — 실패 시 예외를 던져 호출자에서 WAITING 처리 */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void captureTx(Long userId, BigDecimal amount, Long auctionEventId, String description) {
        if (amount == null || amount.signum() <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "INVALID_AMOUNT");
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.debitPoint(amount); // 잔액 부족 시 CustomException(INSUFFICIENT_POINT)
        userRepository.save(user);

        txRepo.save(PointTransaction.builder()
                .user(user)
                .transEnum(TransEnum.CAPTURE)
                .amount(amount.negate())
                .balanceAfter(user.getPoint())
                .description(description)
                .auctionEventId(auctionEventId)
                .build());
    }

    /**포인트 환불 처리*/
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refundTx(Long userId, BigDecimal amount, Long auctionEventId, String description) {
        if (amount == null || amount.signum() <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "INVALID_AMOUNT");
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.creditPoint(amount);
        userRepository.save(user);

        txRepo.save(PointTransaction.builder()
                .user(user)
                .transEnum(TransEnum.REFUND)
                .amount(amount)
                .balanceAfter(user.getPoint())
                .description(description)
                .auctionEventId(auctionEventId)
                .build());
    }
}