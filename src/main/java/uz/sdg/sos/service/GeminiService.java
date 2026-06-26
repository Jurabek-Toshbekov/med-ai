package uz.sdg.sos.service;

import uz.sdg.sos.dto.dmed.DmedPatientData;
import uz.sdg.sos.dto.labevent.LabAnalysisResult;

public interface GeminiService {
    Integer analyzeUrgency(DmedPatientData patient, String diagnosis, String conclusion);

    LabAnalysisResult analyzeLabResult(String testName, String testResult, String unit, String referenceRange);
}
