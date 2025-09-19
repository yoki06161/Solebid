package com.sesac.solbid.repository.order;

import com.sesac.solbid.domain.OrderInfo;
import com.sesac.solbid.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderInfo, Long> {

    List<OrderInfo> findByWinner(User winner);

    List<OrderInfo> findBySeller(User seller);
}
