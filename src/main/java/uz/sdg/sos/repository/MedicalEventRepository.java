package uz.sdg.sos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.sdg.sos.entity.MedicalEventEntity;
import uz.sdg.sos.entity.enums.MedicalEventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalEventRepository extends JpaRepository<MedicalEventEntity, UUID> {
    List<MedicalEventEntity> findAllByStatus(MedicalEventStatus status);
    List<MedicalEventEntity> findAllByClinicId(UUID clinicId);
    List<MedicalEventEntity> findAllByNotifyAtBeforeAndStatus(LocalDateTime notifyAt, MedicalEventStatus status);
}
