package uz.sdg.sos.dto.makeObjects;

import lombok.*;
import uz.sdg.sos.base.BaseDto;
import uz.sdg.sos.dto.FileDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDtoMake extends BaseDto {
    private Long id;
    private String name;
    private Long size;
    private String extension;
}
