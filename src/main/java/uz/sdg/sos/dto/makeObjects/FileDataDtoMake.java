package uz.sdg.sos.dto.makeObjects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uz.sdg.sos.dto.FileDto;


@Getter
@Setter
@RequiredArgsConstructor
public class FileDataDtoMake extends FileDto {
    private Boolean requiredFile;
    private String requiredFileName;
    private Boolean editEnable = true;

    public FileDataDtoMake(Boolean requiredFile, String requiredFileName) {
        this.requiredFile = requiredFile;
        this.requiredFileName = requiredFileName;
    }

}
