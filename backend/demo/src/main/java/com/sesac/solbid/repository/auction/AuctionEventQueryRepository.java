package com.sesac.solbid.repository.auction;

import com.sesac.solbid.domain.AuctionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AuctionEventQueryRepository extends JpaRepository<AuctionEvent, Long> {

    @Query("""
      select a.auctionEventId
      from AuctionEvent a
      where a.status = com.sesac.solbid.domain.enums.AuctionStatus.LIVE
        and a.endAt <= :now
    """)
    List<Long> findLiveAuctionsEndedBefore(LocalDateTime now);
}
