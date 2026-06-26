package uz.sdg.sos.dto.makeObjects;

import uz.sdg.sos.base.BaseDto;

public class Commodity extends BaseDto {
    private String name;
    private Integer price;


    public Commodity() {
    }

    public Commodity(String name, Integer price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
