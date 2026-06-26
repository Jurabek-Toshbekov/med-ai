package uz.sdg.sos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.sdg.sos.entity.ClinicEntity;
import uz.sdg.sos.entity.enums.ClinicStatus;
import uz.sdg.sos.entity.enums.ClinicType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClinicRepository extends JpaRepository<ClinicEntity, UUID> {

    Optional<ClinicEntity> findBySecretKey(String secretKey);

    List<ClinicEntity> findAllByStatus(ClinicStatus status);

    long countByStatus(ClinicStatus status);
    long countByClinicType(ClinicType clinicType);
    long countByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT c FROM ClinicEntity c WHERE c.secretKeyExpiresAt IS NOT NULL AND c.secretKeyExpiresAt BETWEEN :now AND :threshold AND c.status = uz.sdg.sos.entity.enums.ClinicStatus.ACTIVE")
    List<ClinicEntity> findExpiringKeys(@Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold);
}
