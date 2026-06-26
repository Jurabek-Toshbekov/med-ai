package uz.sdg.sos.dto.labevent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.sdg.sos.entity.enums.LabResultFlag;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabAnalysisResult {
    private LabResultFlag flag;
    private Integer hoursUntilContact;
    private String conclusion;
}
