package com.sesac.solbid.repository;

import com.sesac.solbid.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    Optional<User> findByPhone(String phone);

    //user point update
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.point = u.point + :delta where u.userId = :userId")
    int addPoint(@Param("userId") Long userId, @Param("delta") BigDecimal delta);
}
