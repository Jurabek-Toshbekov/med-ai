package uz.sdg.sos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.sdg.sos.dto.makeObjects.FileDtoMake;
import uz.sdg.sos.entity.FileEntity;
import uz.sdg.sos.entity.enums.FileType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FileDto {
    private Long id;
    private String name;
    private String type;
    private Long size;
    private String extension;
    private String hashId;
    private String contentType;
    private String uploadPath;
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    public static FileDtoMake fileDtoMake(FileEntity fileEntity,FileDtoMake make){
        make.setId(fileEntity.getId());
        make.setName(fileEntity.getName());
        make.setSize(fileEntity.getSize());
        make.setExtension(fileEntity.getExtension());
        return make;
    }


    public static FileDto toDto(FileEntity entity, FileDto dto) {
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setSize(entity.getSize());
        dto.setExtension(entity.getExtension());
        dto.setHashId(entity.getHashId());
        dto.setContentType(entity.getContentType());
        dto.setFileType(entity.getFileType());
        return dto;
    }


    public static FileDto fileDto(FileEntity entity) {
        return FileDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .size(entity.getSize())
                .extension(entity.getExtension())
                .build();
    }

    public static FileDto toDto(FileEntity entity) {
        return toDto(entity, new FileDto());
    }


}
