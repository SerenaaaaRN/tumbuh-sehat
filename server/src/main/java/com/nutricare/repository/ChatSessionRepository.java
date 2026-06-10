package com.stuntingai.repository;
import com.stuntingai.domain.entities.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
    Optional<ChatSession> findByPredictionId(String predictionId);
    boolean existsByPredictionId(String predictionId);
}
