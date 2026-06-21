package com.nutricare.repository;
import com.nutricare.domain.entity.VerifiableCredential;
import com.nutricare.domain.enums.VcType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface VerifiableCredentialRepository extends JpaRepository<VerifiableCredential, String> {
    List<VerifiableCredential> findByChildId(String childId);
    List<VerifiableCredential> findByChildIdAndIsRevokedFalse(String childId);
    Optional<VerifiableCredential> findByIpfsCid(String ipfsCid);
    Optional<VerifiableCredential> findByTxHash(String txHash);
    List<VerifiableCredential> findByIssuerId(String issuerId);
    boolean existsByChildIdAndVcTypeAndIsRevokedFalse(String childId, VcType vcType);
    @Query("SELECT vc FROM VerifiableCredential vc WHERE vc.child.id = :childId AND vc.isRevoked = false AND (vc.expiresAt IS NULL OR vc.expiresAt > CURRENT_TIMESTAMP)")
    List<VerifiableCredential> findValidByChildId(@Param("childId") String childId);
}
