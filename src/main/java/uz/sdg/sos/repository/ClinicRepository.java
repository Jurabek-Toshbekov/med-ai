package uz.sdg.sos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.sdg.sos.entity.ClinicEntity;
import uz.sdg.sos.entity.enums.ClinicStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClinicRepository extends JpaRepository<ClinicEntity, UUID> {

    Optional<ClinicEntity> findBySecretKey(String secretKey);

    List<ClinicEntity> findAllByStatus(ClinicStatus status);
}
