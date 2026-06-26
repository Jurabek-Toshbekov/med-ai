package uz.sdg.sos.controller;

import org.springframework.core.io.FileUrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.entity.FileEntity;
import uz.sdg.sos.service.FileService;
import uz.sdg.sos.service.impl.FileServiceImpl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequestMapping("/sdg/uz")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }


    @PostMapping("upload")
//    @PreAuthorize(value = "hasAnyAuthority('SUPER_ADMIN', 'ADMIN','BUSINESSMAN','CLIENT','COMMENTER')")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam Long userId,
            @RequestParam String category,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.controller(fileService.uploadPhoto(userId, category, file));
    }

//    @PostMapping("upload/many")
//// @PreAuthorize(value = "hasAnyAuthority('SUPER_ADMIN', 'ADMIN','BUSINESSMAN','CLIENT','COMMENTER')")
//    public ResponseEntity<?> uploadPhotos(
//            @RequestParam Long userId,
//            @RequestParam String category,
//            @RequestParam("files") MultipartFile[] files) { // Bir nechta fayllar qabul qilinadi
//        return ApiResponse.controller(fileService.uploadPhotos(userId, category, files));
//    }

    @DeleteMapping("delete")
//    @PreAuthorize(value = "hasAnyAuthority('SUPER_ADMIN', 'ADMIN','BUSINESSMAN','CLIENT')")
    public ResponseEntity<?> deleteFile(@RequestParam Long userId, @RequestParam String fileHashId) {
        return ApiResponse.controller(fileService.deleteFile(userId, fileHashId));
    }


    @GetMapping("/view/one/photo")
//    @PreAuthorize(value = "hasAnyAuthority('SUPER_ADMIN', 'ADMIN','BUSINESSMAN','CLIENT','COMMENTER','GUEST')")
    public ResponseEntity<?> getOnePhoto(@RequestParam Long id) throws IOException {
        if (id == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Bo'sh qiymat qabul qilinmaydi .");
        }
        Optional<FileEntity> optionalFile = fileService.getById(id);
        if (optionalFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Fayl topilmadi ");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; fileName=\"" + URLEncoder.encode(optionalFile.get().getUploadPath(), StandardCharsets.UTF_8))
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(optionalFile.get().getSize())
                .body(new FileUrlResource(String.format(optionalFile.get().getUploadPath())));
    }

    @GetMapping("get/one/file")
    public ResponseEntity<?> getOneFile(@RequestParam Long id) {
        if (id == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Bo'sh qiymat qabul qilinmaydi .");
        }
        try {
            Optional<FileEntity> optionalFile = fileService.getById(id);
            if (optionalFile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Fayl topilmadi ");
            }

            String extension = optionalFile.get().getExtension().toLowerCase();
            String mimeType = FileServiceImpl.fileFormatToMimeType.getOrDefault(extension, "application/octet-stream"); // Standart MIME tur
            MediaType mediaType = MediaType.valueOf(mimeType);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; fileName=\"" + URLEncoder.encode(optionalFile.get().getName(), StandardCharsets.UTF_8))
                    .contentType(mediaType)
                    .contentLength(optionalFile.get().getSize())
                    .body(new FileUrlResource(optionalFile.get().getUploadPath()));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Xatolik ___ " + e.getMessage());
        }

    }


    @GetMapping("/get/file-info2")
//    @PreAuthorize(value = "hasAnyAuthority('SUPER_ADMIN', 'ADMIN','BUSINESSMAN','CLIENT','COMMENTER','GUEST')")
    public ResponseEntity<?> getFileInfo2(@RequestParam Long id) {
        return ResponseEntity.ok(fileService.getByIdOnlyFileData(id));
    }


    @GetMapping("/down/one/photo")
//    @PreAuthorize(value = "hasAnyAuthority('SUPER_ADMIN', 'ADMIN','BUSINESSMAN','CLIENT','COMMENTER','GUEST')")
    public ResponseEntity<?> downOnePhoto(@RequestParam String hashId) throws IOException {
        if (hashId == null || hashId.equals("")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Bo'sh qiymat qabul qilinmaydi .");
        }
        Optional<FileEntity> optionalFile = fileService.findByHashId(hashId);
        if (optionalFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Fayl topilmadi ");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=\"" + URLEncoder.encode(optionalFile.get().getUploadPath(), StandardCharsets.UTF_8))
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(optionalFile.get().getSize())
                .body(new FileUrlResource(String.format(optionalFile.get().getUploadPath())));
    }

    @GetMapping("get/all")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ApiResponse.controller(fileService.getAll(page, size));
    }



}
