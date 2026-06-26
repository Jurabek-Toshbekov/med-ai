package uz.sdg.sos.dto.makeObjects;

import uz.sdg.sos.base.BaseDto;
import uz.sdg.sos.base.SerializableImpl;

public class ReferralCountDTOMake extends BaseDto {
    private Long refUserId;
    private Integer count;

    public ReferralCountDTOMake(Long refUserId, Integer count) {
        this.refUserId = refUserId;
        this.count = count;
    }

    public Long getRefUserId() {
        return refUserId;
    }

    public void setRefUserId(Long refUserId) {
        this.refUserId = refUserId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public ReferralCountDTOMake() {
    }
}
