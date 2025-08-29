package com.sesac.solbid.domain;

import com.sesac.solbid.domain.enums.ProviderType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name="social_login")
public class SocialLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long socialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ProviderType provider;

    @Column(length = 100, nullable = false)
    private String providerId;

    // 제공자 토큰 저장: Google revoke 자동화를 위해 사용
    @Column(length = 2048)
    private String providerAccessToken;

    @Column(length = 2048)
    private String providerRefreshToken;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public SocialLogin(User user, ProviderType provider, String providerId) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
    }

    public void updateProviderTokens(String accessToken, String refreshToken) {
        this.providerAccessToken = accessToken;
        this.providerRefreshToken = refreshToken;
    }
}
