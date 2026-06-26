package uz.sdg.sos.service;

import uz.sdg.sos.entity.ClinicEntity;
import uz.sdg.sos.entity.LabEntity;
import uz.sdg.sos.entity.LabEventEntity;
import uz.sdg.sos.entity.MedicalEventEntity;

public interface DmedSyncService {

    void syncToDmed(MedicalEventEntity event, ClinicEntity clinic);

    void syncToDmed(LabEventEntity event, LabEntity lab);
}
