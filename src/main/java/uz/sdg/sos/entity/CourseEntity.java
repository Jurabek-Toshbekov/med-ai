package uz.sdg.sos.entity;


import lombok.Getter;
import lombok.Setter;
import uz.sdg.sos.base.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@Entity(name = "courses")
public class CourseEntity extends BaseEntity  {
    @Column(nullable = false)
    private String name;
    private Long iconId;
}
