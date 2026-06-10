package com.nutricare.repository;
import com.nutricare.domain.entity.Prediction;
import com.nutricare.domain.enums.PredictionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface PredictionRepository extends JpaRepository<Prediction, String> {
    Optional<Prediction> findByAssessmentId(String assessmentId);
    Optional<Prediction> findFirstByAssessmentChildIdOrderByCreatedAtDesc(String childId);
    List<Prediction> findByPredictionStatus(PredictionStatus status);
    boolean existsByAssessmentId(String assessmentId);
}
