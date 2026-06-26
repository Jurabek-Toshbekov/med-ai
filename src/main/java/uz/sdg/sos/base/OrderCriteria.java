package uz.sdg.sos.base;

import org.springframework.data.domain.Sort;

public class OrderCriteria extends BaseDto {

    private String key;
    private Sort.Direction direction;

    public static OrderCriteria of(String key, Sort.Direction direction) {
        return new OrderCriteria(key, direction);
    }

    public OrderCriteria(String key, Sort.Direction direction) {
        this.key = key;
        this.direction = direction;
    }

    public OrderCriteria() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Sort.Direction getDirection() {
        return direction;
    }

    public void setDirection(Sort.Direction direction) {
        this.direction = direction;
    }
}
