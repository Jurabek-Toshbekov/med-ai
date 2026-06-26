package uz.sdg.sos.dto.makeObjects;


import java.io.Serializable;
import java.util.Objects;

public class FCMTokenMake implements Serializable {


    private String deviceId;
    private String token;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public FCMTokenMake() {
    }

    public FCMTokenMake(String deviceId, String token) {
        this.deviceId = deviceId;
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FCMTokenMake that = (FCMTokenMake) o;
        return Objects.equals(deviceId, that.deviceId) && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, token);
    }
}
