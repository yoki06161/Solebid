package com.sesac.solbid.config;

import com.sesac.solbid.repository.auction.AuctionEventQueryRepository;
import com.sesac.solbid.service.auction.AuctionSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

/**
 * 스케줄링 설정
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {

    private final AuctionEventQueryRepository queryRepo;
    private final AuctionSettlementService settlement;

    @Scheduled(fixedDelay = 300_000) /*테스트용, 후에 15000으로 변경 예정*/
    public void sweep() {
        var now = LocalDateTime.now();
        for (Long id : queryRepo.findLiveAuctionsEndedBefore(now)) {
            try {
                // 건별 새 트랜잭션으로 실행
                settlement.finalizeIfDueTx(id, now);
            } catch (Exception e) {
                // 한 건 실패해도 다음 건 계속
                System.err.println("Finalize failed auctionId=" + id + " : " + e.getMessage());
            }
        }
    }

}