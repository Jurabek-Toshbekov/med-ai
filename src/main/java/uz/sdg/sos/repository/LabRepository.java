package uz.sdg.sos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.sdg.sos.entity.LabEntity;
import uz.sdg.sos.entity.enums.ClinicStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabRepository extends JpaRepository<LabEntity, UUID> {

    Optional<LabEntity> findBySecretKey(String secretKey);

    long countByStatus(ClinicStatus status);

    List<LabEntity> findAllByStatus(ClinicStatus status);
}
