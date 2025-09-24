package com.sesac.solbid.repository;

import com.sesac.solbid.domain.Notification;
import com.sesac.solbid.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByUserAndIsReadFalse(User user);
    Page<Notification> findByUserOrderByCreateAtDesc(User user, Pageable pageable);
}