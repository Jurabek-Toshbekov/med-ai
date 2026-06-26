package uz.sdg.sos.service;

import uz.sdg.sos.entity.MedicalEventEntity;

public interface NotificationService {
    void scheduleNotification(MedicalEventEntity event);
}
