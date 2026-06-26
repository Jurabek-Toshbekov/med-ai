package uz.sdg.sos.service;

public interface SmsService {

    void sendOtp(String phoneNumber, String code);

    void sendNotification(String phoneNumber, String patientFullName, int hours);
}
