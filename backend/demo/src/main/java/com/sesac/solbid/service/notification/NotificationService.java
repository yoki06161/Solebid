package com.sesac.solbid.service.notification;

import com.sesac.solbid.domain.User;

public interface NotificationService {
    void notifyOutbid(User receiver, Long auctionEventId, String productName, String currentPriceKr, String myBidKr);
}
