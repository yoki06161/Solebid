package com.sesac.solbid.domain;

import com.sesac.solbid.domain.enums.UserStatus;
import com.sesac.solbid.domain.enums.UserType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(length = 50, unique = true, nullable = false)
    private String nickname;

    @Column(length = 50)
    private String name;

    @Column(length = 20, unique = true, nullable = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserType userType;

    @Column(nullable = false)
    @ColumnDefault("36.5")
    private BigDecimal temperature;

    @Column(nullable = false)
    @ColumnDefault("0")
    private BigDecimal point;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus userStatus;

    @Column
    private LocalDateTime withdrawnAt;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean emailVerified;

    @Column
    private LocalDateTime emailVerifiedAt;

    //== 연관관계 편의 메서드 ==//
    @OneToMany(mappedBy = "user")
    private List<SocialLogin> socialLogins = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<PointTransaction> pointTransactions = new ArrayList<>();

    // @OneToMany(mappedBy = "seller")
    // private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "bidder")
    private List<Bid> bids = new ArrayList<>();
    @OneToMany(mappedBy = "winner")
    private List<OrderInfo> winnerOrders = new ArrayList<>();
    @OneToMany(mappedBy = "seller")
    private List<OrderInfo> sellerOrders = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<Wish> wishes = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<Notification> notifications = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<Carts> carts = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    private List<Payments> payments = new ArrayList<>();

    @Builder
    public User(String email, String password, String nickname, String name, String phone) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.name = name;
        this.phone = phone;
        this.userType = UserType.USER;
        this.temperature = new BigDecimal("36.5");
        this.point = BigDecimal.ZERO;
        this.userStatus = UserStatus.ACTIVE;
        this.emailVerified = false;
        // withdrawnAt, emailVerifiedAt 기본값은 null
    }

    // 닉네임 동기화용 업데이트 메서드
    public void updateNickname(String newNickname) {
        if (newNickname != null && !newNickname.isBlank() && !newNickname.equals(this.nickname)) {
            this.nickname = newNickname;
        }
    }

    // 비밀번호 변경 메서드
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 회원 탈퇴(소프트 삭제): 상태 전환 + 일시 기록
    public void withdraw() {
        if (this.userStatus != UserStatus.WITHDRAWN) {
            this.userStatus = UserStatus.WITHDRAWN;
            this.withdrawnAt = LocalDateTime.now();
        }
    }

    // 회원 재활성화: 상태/일시 원복
    public void reactivate() {
        if (this.userStatus == UserStatus.WITHDRAWN) {
            this.userStatus = UserStatus.ACTIVE;
            this.withdrawnAt = null;
        }
    }

    // 이메일 인증 처리
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    // UserDetails Impl
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.userType.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.userStatus != UserStatus.BLOCKED;
    }

    @Override
    public boolean isEnabled() {
        return this.userStatus == UserStatus.ACTIVE;
    }
}
