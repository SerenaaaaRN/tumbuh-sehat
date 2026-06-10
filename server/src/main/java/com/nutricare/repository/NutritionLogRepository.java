package com.nutricare.repository;
import com.nutricare.domain.entity.NutritionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface NutritionLogRepository extends JpaRepository<NutritionLog, String> {
    Page<NutritionLog> findByChildIdOrderByCreatedAtDesc(String childId, Pageable pageable);
    List<NutritionLog> findTop7ByChildIdOrderByCreatedAtDesc(String childId);
    long countByChildId(String childId);
}
