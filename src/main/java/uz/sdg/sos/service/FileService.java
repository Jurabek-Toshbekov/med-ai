package uz.sdg.sos.service;

import org.springframework.web.multipart.MultipartFile;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.FileDto;
import uz.sdg.sos.dto.makeObjects.FileDtoMake;
import uz.sdg.sos.entity.FileEntity;

import java.util.List;
import java.util.Optional;

public interface FileService {


    ApiResponse<?> uploadPhoto(Long userId,  String category ,MultipartFile multipartFile);

   Optional<FileEntity> findByHashId(String hashId);
   Optional<FileEntity> getById(Long id);

    ApiResponse<?> getFileDataByHashId(String hashId);

    ApiResponse<?>deleteFile(Long userId, String fileHashId);
    boolean deleteFileOnlySystem( String fileHashId);
    boolean deleteById( Long fileId);
    boolean available ( Long fileId);

    Boolean filetypeDraftToActive(String fileHashId);
    Boolean filetypeDraftToActiveById(Long fileId);

    FileDtoMake getByIdOnlyFileData(Long fileId);
    List<FileDtoMake> getByIdOnlyFileDataList(List<Long> ids);

    boolean existById (Long fileId);

    ApiResponse<?> getAll (int page , int size);
}
