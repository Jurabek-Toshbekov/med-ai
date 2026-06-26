package uz.sdg.sos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.FileDto;
import uz.sdg.sos.dto.makeObjects.FileDtoMake;
import uz.sdg.sos.entity.FileEntity;
import uz.sdg.sos.entity.UserEntity;
import uz.sdg.sos.entity.enums.AccountTypeEnum;
import uz.sdg.sos.entity.enums.FileType;
import uz.sdg.sos.repository.FileRepository;
import uz.sdg.sos.repository.UserRepository;
import uz.sdg.sos.service.FileService;
import uz.sdg.sos.utils.ResMessages;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final UserRepository userRepository;


    String UPLOAD_FILE_URL = "sdg/uz";  // Ubuntu 20.04 URL
    Long MAX_FILE_SIZE = 15728640L;  // upload max size 15MB


    @Override
    public ApiResponse<?> uploadPhoto(Long userId, String category, MultipartFile multipartFile) {
        try {
            if (userId == null || category == null || multipartFile.isEmpty()) {
                return new ApiResponse<>(false, ResMessages.OBJECT_IS_NULL);
            }
            if (!isValidFileCategory(category)) {
                return new ApiResponse<>(false, "category xato");
            }

            if (multipartFile.getSize() > MAX_FILE_SIZE) {
                return new ApiResponse<>(false, "Fayl hajmi '15 MB'dan katta");
            }

            String fileFormat = getExt(multipartFile.getOriginalFilename());
            if (!isValidFileFormat(fileFormat)) {
                return new ApiResponse<>(false, ResMessages.FILE_FORMAT_ERR);
            }

            Optional<UserEntity> userEntityOptional = userRepository.findById(userId);
            if (userEntityOptional.isEmpty()) {
                return new ApiResponse<>(false, ResMessages.USER_NOT_FOUND);
            }
            String educationCenterName = "education";

            String userName = userEntityOptional.get().getFirstName() + userEntityOptional.get().getId();
            userName = userName.replaceAll("[^a-zA-Z0-9]", "");

            File uploadFolder;
            if (educationCenterName != null) {
                uploadFolder = new File(String.format("%s/%s", UPLOAD_FILE_URL, category + "/" + educationCenterName + "/" + userName));
            } else {
                uploadFolder = new File(String.format("%s/%s", UPLOAD_FILE_URL, category));
            }

            if (!uploadFolder.exists()) {
                if (uploadFolder.mkdirs()) {
                    System.out.println("Ushbu yol yaratildi: " + uploadFolder.getPath());
                } else {
                    return new ApiResponse<>(false, "Rasmni saqlash uchun quti yarata olmadi.");
                }
            }

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            String formattedDateTime = now.format(formatter);
            String fileName = userName + "_" + formattedDateTime;
            uploadFolder = uploadFolder.getAbsoluteFile();
            File file = new File(uploadFolder, String.format("%s.%s", fileName, fileFormat));

            FileEntity fileEntity = new FileEntity();
            fileEntity.setCreatedBy(userId);
            fileEntity.setName(fileName);
            fileEntity.setOrginalName(multipartFile.getOriginalFilename());
            fileEntity.setExtension(fileFormat);
            fileEntity.setHashId(UUID.randomUUID().toString());
            fileEntity.setSize(multipartFile.getSize());
            fileEntity.setContentType(multipartFile.getContentType());
            fileEntity.setUploadPath(uploadFolder.getPath() + "/" + fileName + "." + fileFormat);
            fileEntity.setFileType(FileType.DRAFT);
            fileEntity.setFileCategory(category);
            fileRepository.save(fileEntity);

            try {
                multipartFile.transferTo(file);
            } catch (Exception e) {
                return new ApiResponse<>(false, "Rasmni saqlashda xatolik yuz berdi");
            }
            // response data
            HashMap<String, Object> response = new HashMap<>();
            response.put("id", fileEntity.getId());
            response.put("hashId", fileEntity.getHashId());
            response.put("name", fileEntity.getName());
            response.put("orginalName", fileEntity.getOrginalName());
            response.put("type", fileEntity.getType());
            response.put("size", fileEntity.getSize());
            response.put("extension", fileEntity.getExtension());
            response.put("contentType", fileEntity.getContentType());
            response.put("uploadPath", fileEntity.getUploadPath());
            response.put("fileCategory", fileEntity.getFileCategory());

            return new ApiResponse<>(true, ResMessages.SUCCESS, response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }


    @Override
    public Optional<FileEntity> findByHashId(String hashId) {
        if (hashId != null) {
            Optional<FileEntity> optionalFile = fileRepository.findByHashId(hashId);
            if (optionalFile.isPresent()) {
                return optionalFile;
            }
            return Optional.empty();
        } else return Optional.empty();
    }

    @Override
    public Optional<FileEntity> getById(Long id) {
        if (id != null) {
            Optional<FileEntity> optionalFile = fileRepository.findById(id);
            if (optionalFile.isPresent()) {
                return optionalFile;
            }
            return Optional.empty();
        } else return Optional.empty();
    }

    @Override
    public ApiResponse<?> getFileDataByHashId(String hashId) {
        try {
            Optional<FileEntity> optionalFile = fileRepository.findByHashId(hashId);
            if (!fileRepository.existsByHashId(hashId) || optionalFile.isEmpty()) {
                return new ApiResponse<>(false, ResMessages.OBJECT_NOT_FOUND);
            }
            return new ApiResponse<>(true, ResMessages.SUCCESS, FileDto.toDto(optionalFile.get()));
        } catch (Throwable e) {
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<?> deleteFile(Long userId, String fileHashId) {
        try {
            if (userId == null || fileHashId == null) {
                return new ApiResponse<>(false, ResMessages.OBJECT_IS_NULL);
            }
            Optional<FileEntity> optionalFile = fileRepository.findByHashId(fileHashId);
            if (optionalFile.isEmpty()) {
                return new ApiResponse<>(false, ResMessages.OBJECT_NOT_FOUND);
            }
            Optional<UserEntity> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return new ApiResponse<>(false, ResMessages.OBJECT_NOT_FOUND);
            }
            if ((!optionalUser.get().getAccountType().equals(AccountTypeEnum.ADMIN)) && (!optionalFile.get().getCreatedBy().equals(userId))) {
                return new ApiResponse<>(false, ResMessages.NOT_CREATOR);
            }

            File file = new File(optionalFile.get().getUploadPath());
            if (file.exists()) {
                if (file.delete()) {
                    try {
                        fileRepository.delete(optionalFile.get());
                        System.out.println("file o'chirildi");
                        return new ApiResponse<>(true, ResMessages.SUCCESS);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new ApiResponse<>(false, "Rasmni o'chirishda xatolik yuzaga keldi");
                    }
                } else {
                    return new ApiResponse<>(false, "Rasmni o'chirishda xatolik yuzaga keldi");
                }
            } else {
                return new ApiResponse<>(false, "O'chirish kerak bo'lgan fayl bazada topilmadi ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Override
    public boolean deleteFileOnlySystem(String fileHashId) {
        try {
            if (fileHashId == null) {
                return false;
            }
            Optional<FileEntity> optionalFile = fileRepository.findByHashId(fileHashId);
            if (optionalFile.isEmpty()) {
                return false;
            }

            File file = new File(optionalFile.get().getUploadPath());
            if (file.exists()) {
                if (file.delete()) {
                    try {
                        fileRepository.delete(optionalFile.get());
                        System.out.println("file o'chirildi");
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(Long fileId) {
        try {
            if (fileId == null) {
                return false;
            }
            Optional<FileEntity> optionalFile = fileRepository.findById(fileId);
            if (optionalFile.isEmpty()) {
                return false;
            }
            return deleteFileOnlySystem(optionalFile.get().getHashId());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean available(Long fileId) {
        try {
            return fileRepository.existsById(fileId);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Boolean filetypeDraftToActive(String fileHashId) {
        try {
            if (fileHashId == null) {
                return false;
            }
            Optional<FileEntity> optionalFile = fileRepository.findByHashId(fileHashId);
            if (optionalFile.isEmpty()) {
                return false;
            }
            if (optionalFile.get().getFileType().equals(FileType.DRAFT)) {
                optionalFile.get().setFileType(FileType.ACTIVE);
                fileRepository.save(optionalFile.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean filetypeDraftToActiveById(Long fileId) {
        try {
            if (fileId == null) {
                return false;
            }
            Optional<FileEntity> optionalFile = fileRepository.findById(fileId);
            if (optionalFile.isEmpty()) {
                return false;
            }
            if (optionalFile.get().getFileType().equals(FileType.DRAFT)) {
                optionalFile.get().setFileType(FileType.ACTIVE);
                fileRepository.save(optionalFile.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public FileDtoMake getByIdOnlyFileData(Long fileId) {
        try {
            if (fileId == null) {
                return null;
            }
            Optional<FileEntity> optionalFile = fileRepository.findById(fileId);
            if (optionalFile.isEmpty()) {
                return null;
            }
            FileDtoMake make = FileDto.fileDtoMake(optionalFile.get(), new FileDtoMake());
            return make;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<FileDtoMake> getByIdOnlyFileDataList(List<Long> ids) {
        try {
            if (ids == null) {
                return null;
            }
            List<FileDtoMake> response = new ArrayList<>();
            for (Long id : ids) {
                FileDtoMake data = getByIdOnlyFileData(id);
                if (data != null) {
                    response.add(data);
                }
            }
            return response;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean existById(Long fileId) {
        try {
            return fileRepository.existsById(fileId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ApiResponse<?> getAll(int page, int size) {
        try {
            PageRequest pageR = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            fileRepository.findAll(pageR);
            return new ApiResponse<>(true, ResMessages.SUCCESS ,fileRepository.findAll(pageR));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }


    public void deleteTrashFiles() {
        try {
            LocalDateTime localTime = LocalDateTime.now();
            LocalDateTime oneDayOld = localTime.minus(24, ChronoUnit.HOURS);
            List<FileEntity> all = fileRepository.findAllByCreatedAtBeforeAndFileType(oneDayOld, FileType.DRAFT);
            if (all.size() > 0) {
                for (FileEntity fileEntity : all) {
                    File file = new File(fileEntity.getUploadPath());
                    if (file.exists()) {
                        if (file.delete()) {
                            try {
                                fileRepository.delete(fileEntity);
                                System.out.println("file o'chirildi");
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("file o'chirishda xatolik");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // faylni formatini olish
    private String getExt(String fileName) {
        String ext = null;
        if (fileName != null && !fileName.isEmpty()) {
            int dot = fileName.lastIndexOf(".");
            if (dot > 0 && dot <= fileName.length() - 2) {
                ext = fileName.substring(dot + 1);
            }
        }
        return ext;
    }

    private static final String[] validCategory = {
            "admin_category",
            "user_avatar",
            "user_document",
            "education_theme",
            "education_icon",
            "chat",
            "quiz"
    };

    public static boolean isValidFileCategory(String fileCategory) {
        for (String file : validCategory) {
            if (fileCategory.equals(file)) {
                return true;
            }
        }
        return false;
    }

    private static final String[] validFileFormats = {
            // Rasm formatlari
            "jpg",
            "jpeg",
            "png",
            "gif",
            "bmp",
            "webp",
            "tiff",
            "svg",
            "ico",

            // Hujjat formatlari
            "pdf",
            "doc",
            "docx",
            "xls",
            "xlsx",
            "ppt",
            "pptx",

            // audio
            "mp3",
            "wav",
            "ogg"
    };

    public static boolean isValidFileFormat(String fileExtension) {
        for (String format : validFileFormats) {
            if (fileExtension.equalsIgnoreCase(format)) {  // Katta va kichik harflarni hisobga olmaslik uchun equalsIgnoreCase ishlatiladi
                return true;
            }
        }
        return false;
    }

    public static final Map<String, String> fileFormatToMimeType = new HashMap<>() {{
        // Rasm formatlari
        put("jpg", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("png", "image/png");
        put("gif", "image/gif");
        put("bmp", "image/bmp");
        put("webp", "image/webp");
        put("tiff", "image/tiff");
        put("svg", "image/svg+xml");
        put("ico", "image/x-icon");

        // Hujjat formatlari
        put("pdf", "application/pdf");
        put("doc", "application/msword");
        put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        put("xls", "application/vnd.ms-excel");
        put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        put("ppt", "application/vnd.ms-powerpoint");
        put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");

        // Audio formatlari
        put("mp3", "audio/mpeg");
        put("wav", "audio/wav");
        put("ogg", "audio/ogg");
    }};


}
