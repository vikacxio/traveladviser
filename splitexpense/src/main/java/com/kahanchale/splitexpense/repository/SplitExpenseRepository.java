oooopackage com.kahanchale.splitexpense.repository;

import com.kahanchale.splitexpense.entity.SplitExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SplitExpenseRepository extends JpaRepository<SplitExpense, Long> {

    List<SplitExpense> findByGroupId(Long groupId);
}