package uz.sdg.sos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.sdg.sos.entity.ClinicApplicationEntity;
import uz.sdg.sos.entity.enums.ClinicApplicationStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClinicApplicationRepository extends JpaRepository<ClinicApplicationEntity, UUID> {

    List<ClinicApplicationEntity> findAllByStatusOrderByCreatedAtDesc(ClinicApplicationStatus status);
    boolean existsByLogin(String login);
}
