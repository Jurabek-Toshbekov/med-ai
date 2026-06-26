package uz.sdg.sos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.sdg.sos.entity.LabEventEntity;
import uz.sdg.sos.entity.enums.LabResultFlag;
import uz.sdg.sos.entity.enums.MedicalEventStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LabEventRepository extends JpaRepository<LabEventEntity, UUID> {

    long countByStatus(MedicalEventStatus status);

    long countByFlag(LabResultFlag flag);

    long countByCreatedAtAfter(LocalDateTime dateTime);
}
