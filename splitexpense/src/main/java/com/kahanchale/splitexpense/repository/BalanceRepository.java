package com.kahanchale.splitexpense.repository;

import com.kahanchale.splitexpense.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {

    Optional<Balance> findByUserIdAndOwesTo(Long userId, Long owesTo);

    List<Balance> findByUserId(Long userId);

    List<Balance> findByOwesTo(Long owesTo);

    @Modifying
    @Query("UPDATE Balance b SET b.amount = b.amount + :amount WHERE b.userId = :userId AND b.owesTo = :owesTo")
    int updateBalance(@Param("userId") Long userId, @Param("owesTo") Long owesTo, @Param("amount") BigDecimal amount);

    @Query("SELECT b FROM Balance b WHERE b.group.id = :groupId")
    List<Balance> findBalancesByGroupId(@Param("groupId") Long groupId);
}