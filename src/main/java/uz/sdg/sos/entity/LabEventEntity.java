package uz.sdg.sos.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import uz.sdg.sos.entity.enums.LabResultFlag;
import uz.sdg.sos.entity.enums.MedicalEventStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lab_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabEventEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(length = 14, nullable = false)
    private String jshshir;

    private String patientFullName;

    private UUID labId;

    @Column(nullable = false)
    private String testName;

    @Column(nullable = false)
    private String testResult;

    private String unit;

    private String referenceRange;

    @Enumerated(EnumType.STRING)
    private LabResultFlag flag;

    private Integer hoursUntilContact;

    @Column(columnDefinition = "text")
    private String conclusion;

    private String familyDoctorId;

    private String familyDoctorPhone;

    private String familyDoctorFcmToken;

    private LocalDateTime notifyAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MedicalEventStatus status = MedicalEventStatus.PENDING;

    @Column(columnDefinition = "text")
    private String aiRawResponse;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
