package com.nutricare.repository;
import com.nutricare.domain.entity.Assessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, String> {
    List<Assessment> findByChildIdOrderByCreatedAtDesc(String childId);
    Page<Assessment> findByChildIdOrderByCreatedAtDesc(String childId, Pageable pageable);
    Optional<Assessment> findFirstByChildIdOrderByCreatedAtDesc(String childId);
    long countByChildId(String childId);
    @Query("SELECT a FROM Assessment a WHERE a.child.id = :childId AND a.createdAt BETWEEN :from AND :to ORDER BY a.createdAt DESC")
    List<Assessment> findByChildIdAndDateRange(@Param("childId") String childId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @Query("SELECT COUNT(a) > 0 FROM Assessment a WHERE a.id = :id AND a.child.user.id = :userId")
    boolean existsByIdAndUserId(@Param("id") String id, @Param("userId") String userId);
}
