package com.sesac.solbid.repository.user;

import com.sesac.solbid.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    /** 포인트 가감(충전/환불)용. 필요 시 사용 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.point = u.point + :delta where u.userId = :userId")
    int addPoint(@Param("userId") Long userId, @Param("delta") BigDecimal delta);

    /** 포인트 변경 전, User 행을 PESSIMISTIC_WRITE 잠금으로 조회 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.userId = :userId")
    Optional<User> findByIdForUpdate(@Param("userId") Long userId);
}