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

/**
 * 사용자 엔티티 클래스
 * <p>
 * 경매 플랫폼의 사용자 정보를 관리하는 엔티티입니다.
 * Spring Security의 UserDetails 인터페이스를 구현하여 인증/인가 기능을 제공합니다.
 * </p>
 */
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

    /**
     * 사용자 엔티티 생성자
     * 
     * @param email 사용자 이메일 주소
     * @param password 암호화된 비밀번호
     * @param nickname 사용자 닉네임
     * @param name 사용자 실명
     * @param phone 사용자 전화번호
     */
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

    /**
     * 사용자 닉네임을 업데이트합니다.
     * 
     * @param newNickname 새로운 닉네임 (null이 아니고 공백이 아니며 기존 닉네임과 다른 경우에만 업데이트)
     */
    public void updateNickname(String newNickname) {
        if (newNickname != null && !newNickname.isBlank() && !newNickname.equals(this.nickname)) {
            this.nickname = newNickname;
        }
    }

    /**
     * 기본 프로필 정보를 업데이트합니다 (닉네임, 이름).
     * 
     * @param newNickname 새로운 닉네임 (null이 아닌 경우에만 업데이트)
     * @param newName 새로운 이름 (null이 아닌 경우에만 업데이트)
     */
    public void updateBasicProfile(String newNickname, String newName) {
        if (newNickname != null && !newNickname.isBlank() && !newNickname.equals(this.nickname)) {
            this.nickname = newNickname;
        }
        if (newName != null && !newName.isBlank() && !newName.equals(this.name)) {
            this.name = newName;
        }
    }

    /**
     * 민감한 프로필 정보를 업데이트합니다 (이메일, 전화번호).
     * 
     * @param newEmail 새로운 이메일 (null이 아닌 경우에만 업데이트)
     * @param newPhone 새로운 전화번호 (null이 아닌 경우에만 업데이트)
     */
    public void updateSensitiveProfile(String newEmail, String newPhone) {
        if (newEmail != null && !newEmail.isBlank() && !newEmail.equals(this.email)) {
            this.email = newEmail;
            // 이메일 변경 시 재인증 필요
            this.emailVerified = false;
            this.emailVerifiedAt = null;
        }
        if (newPhone != null && !newPhone.isBlank() && !newPhone.equals(this.phone)) {
            this.phone = newPhone;
        }
    }

    /**
     * 사용자 프로필 정보를 업데이트합니다 (하위 호환성을 위해 유지).
     * 
     * @param newNickname 새로운 닉네임 (null이 아닌 경우에만 업데이트)
     * @param newName 새로운 이름 (null이 아닌 경우에만 업데이트)
     * @param newPhone 새로운 전화번호 (null이 아닌 경우에만 업데이트)
     */
    public void updateProfile(String newNickname, String newName, String newPhone) {
        updateBasicProfile(newNickname, newName);
        if (newPhone != null && !newPhone.isBlank() && !newPhone.equals(this.phone)) {
            this.phone = newPhone;
        }
    }

    /**
     * 사용자 비밀번호를 변경합니다.
     * 
     * @param encodedPassword 암호화된 새 비밀번호
     */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /**
     * 회원 탈퇴 처리 (소프트 삭제)
     * 사용자 상태를 WITHDRAWN으로 변경하고 탈퇴 일시를 기록합니다.
     */
    public void withdraw() {
        if (this.userStatus != UserStatus.WITHDRAWN) {
            this.userStatus = UserStatus.WITHDRAWN;
            this.withdrawnAt = LocalDateTime.now();
        }
    }

    /**
     * 회원 재활성화 처리
     * 탈퇴된 사용자를 다시 활성 상태로 변경하고 탈퇴 일시를 초기화합니다.
     */
    public void reactivate() {
        if (this.userStatus == UserStatus.WITHDRAWN) {
            this.userStatus = UserStatus.ACTIVE;
            this.withdrawnAt = null;
        }
    }

    /**
     * 이메일 인증 완료 처리
     * 이메일 인증 상태를 true로 변경하고 인증 완료 일시를 기록합니다.
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    /**
     * 이메일 주소 변경
     * 새로운 이메일 주소로 변경하고 인증 상태를 유지합니다.
     * 
     * @param newEmail 새로운 이메일 주소
     */
    public void updateEmail(String newEmail) {
        if (newEmail != null && !newEmail.trim().isEmpty()) {
            this.email = newEmail.trim();
            // 이메일 변경 시에도 인증 상태는 유지 (이미 인증 과정을 거쳤으므로)
            this.emailVerified = true;
            this.emailVerifiedAt = LocalDateTime.now();
        }
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
