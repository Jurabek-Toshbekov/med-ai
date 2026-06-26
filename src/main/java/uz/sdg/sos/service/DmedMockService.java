package uz.sdg.sos.service;

import uz.sdg.sos.dto.dmed.DmedPatientData;

import java.util.Optional;

public interface DmedMockService {
    Optional<DmedPatientData> findByJshshir(String jshshir);
}
