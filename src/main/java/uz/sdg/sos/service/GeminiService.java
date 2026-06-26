package uz.sdg.sos.service;

import uz.sdg.sos.dto.dmed.DmedPatientData;

public interface GeminiService {
    Integer analyzeUrgency(DmedPatientData patient, String diagnosis, String conclusion);
}
