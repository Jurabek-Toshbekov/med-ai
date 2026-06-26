package uz.sdg.sos.service;

public interface PushNotificationService {
    void send(String fcmToken, String title, String body, String eventId);
}
