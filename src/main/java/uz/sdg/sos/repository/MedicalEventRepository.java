package uz.sdg.sos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    long countByStatus(MedicalEventStatus status);
    long countByCreatedAtAfter(LocalDateTime dateTime);
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query(value = "SELECT CAST(created_at AS DATE) as date, COUNT(*) as count FROM medical_events WHERE created_at >= :from GROUP BY CAST(created_at AS DATE) ORDER BY CAST(created_at AS DATE)", nativeQuery = true)
    List<Object[]> countByDayAfter(@Param("from") LocalDateTime from);

    @Query(value = "SELECT CAST(clinic_id AS text), COUNT(*) as cnt FROM medical_events GROUP BY clinic_id ORDER BY cnt DESC LIMIT 5", nativeQuery = true)
    List<Object[]> topClinicsByEventCount();

    long countByHoursUntilContact(Integer hours);
}
