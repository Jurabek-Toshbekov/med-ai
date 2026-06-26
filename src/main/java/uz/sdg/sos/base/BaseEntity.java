package uz.sdg.sos.base;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public abstract class BaseEntity extends SerializableImpl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;


    private Boolean deleted = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public BaseEntity (LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public BaseEntity(Long id){
        this.id = id;
    }

    public BaseEntity() {
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public Boolean getDeleted() {
        return deleted;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }


    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
