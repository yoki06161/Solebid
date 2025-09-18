package com.sesac.solbid.domain;

import com.sesac.solbid.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name="notification")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 수신자 ID

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @ColumnDefault("false")
    private Boolean isRead;

    private LocalDateTime createAt;

    //기본값/시간 자동화
    @PrePersist void prePersist(){ if(isRead==null) isRead=false; if(createAt==null) createAt=LocalDateTime.now(); }

}
