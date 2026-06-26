package uz.sdg.sos.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.sdg.sos.entity.MedicalEventEntity;
import uz.sdg.sos.service.NotificationService;
import uz.sdg.sos.service.PushNotificationService;
import uz.sdg.sos.service.SmsService;

import static uz.sdg.sos.service.impl.EskizSmsServiceImpl.maskName;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SmsService           smsService;
    private final PushNotificationService pushService;

    @Override
    public void scheduleNotification(MedicalEventEntity event) {
        log.info("Notification scheduled → SMS+Push to doctor {} at {} (eventId={}, patient={})",
                event.getFamilyDoctorPhone(),
                event.getNotifyAt(),
                event.getId(),
                event.getPatientFullName());

        sendSms(event);
        sendPush(event);
    }

    private void sendSms(MedicalEventEntity event) {
        try {
            smsService.sendNotification(
                    event.getFamilyDoctorPhone(),
                    event.getPatientFullName(),
                    event.getHoursUntilContact()
            );
        } catch (Exception e) {
            log.error("[SMS] Xatolik (eventId={}): {}", event.getId(), e.getMessage());
        }
    }

    private void sendPush(MedicalEventEntity event) {
        String masked = maskName(event.getPatientFullName());
        String title  = "Bemor uyiga chiqarildi";
        String body   = masked + " uyiga chiqarildi. " +
                        event.getHoursUntilContact() + " soat ichida holati haqida xabar bering.";

        pushService.send(
                event.getFamilyDoctorFcmToken(),
                title,
                body,
                event.getId().toString()
        );
    }
}
