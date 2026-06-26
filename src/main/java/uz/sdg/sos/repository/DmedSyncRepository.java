package uz.sdg.sos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.sdg.sos.entity.DmedSyncEntity;
import uz.sdg.sos.entity.enums.DmedSyncStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DmedSyncRepository extends JpaRepository<DmedSyncEntity, UUID> {

    Optional<DmedSyncEntity> findByMedicalEventId(UUID medicalEventId);
    List<DmedSyncEntity> findAllByStatus(DmedSyncStatus status);

    long countByStatus(DmedSyncStatus status);
    long countByCreatedAtAfter(LocalDateTime dateTime);
    long countByStatusAndCreatedAtAfter(DmedSyncStatus status, LocalDateTime dateTime);
}
