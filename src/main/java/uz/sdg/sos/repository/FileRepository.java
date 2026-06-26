package uz.sdg.sos.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.sdg.sos.base.BaseRepository;
import uz.sdg.sos.entity.FileEntity;
import uz.sdg.sos.entity.enums.FileType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends BaseRepository<FileEntity> {

    Optional<FileEntity>findByHashId(String hashId);
    Optional<FileEntity>findByHashIdAndFileType(String hashId, FileType fileType);
    boolean existsByHashId(String hashId);
    List<FileEntity>findAllByCreatedAtBeforeAndFileType(LocalDateTime createdAt, FileType fileType);

    // Hamma fayllarning o‘lchami yig‘indisi
    @Query("SELECT COALESCE(SUM(f.size), 0) FROM files f")
    long calculateTotalSize();

    // Fayl turi bo‘yicha o‘lcham yig‘indisi
    @Query("SELECT COALESCE(SUM(f.size), 0) FROM files f WHERE f.fileType = :fileType")
    long calculateTotalSizeByFileType(@Param("fileType") FileType fileType);
}
