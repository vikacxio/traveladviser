package com.kahanchale.splitexpense.repository;

import com.kahanchale.splitexpense.entity.SplitExpenseGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SplitExpenseGroupRepository extends JpaRepository<SplitExpenseGroup, Long> {
}