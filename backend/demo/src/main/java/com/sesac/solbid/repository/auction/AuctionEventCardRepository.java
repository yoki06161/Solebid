package com.sesac.solbid.repository.auction;

import com.sesac.solbid.domain.AuctionEvent;
import com.sesac.solbid.domain.enums.AuctionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.EnumSet;
import java.util.List;

@Repository
public class AuctionEventCardRepository {

    private static final EnumSet<AuctionStatus> DEFAULT_STATUSES = EnumSet.of(
            AuctionStatus.READY,
            AuctionStatus.LIVE
    );

    @PersistenceContext
    private EntityManager em;

    public List<AuctionEvent> findActiveWithProduct(int limit) {
        return em.createQuery(
                        "select distinct a from AuctionEvent a " +
                                "join fetch a.product p " +
                                "left join fetch p.productImages imgs " +
                                "where a.status in :statuses " +
                                "order by a.endAt asc",
                        AuctionEvent.class
                )
                .setParameter("statuses", DEFAULT_STATUSES)
                .setMaxResults(limit)
                .getResultList();
    }
}
