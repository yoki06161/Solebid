package com.sesac.solbid.repository.auth;

import com.sesac.solbid.domain.SocialLogin;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {
    Optional<SocialLogin> findByProviderAndProviderId(ProviderType provider, String providerId);
    Optional<SocialLogin> findByUser(User user);
}
