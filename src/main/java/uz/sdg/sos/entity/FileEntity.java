package uz.sdg.sos.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.sdg.sos.base.BaseEntity;
import uz.sdg.sos.entity.enums.FileType;

import javax.persistence.*;
import java.util.UUID;


@Entity(name = "files")
@Getter
@Setter
public class FileEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    private String orginalName;

    @Column(name = "type")
    private String type;

    @Column(name = "size", nullable = false)
    private Long size;

    private String extension;

    private String hashId;

    private String contentType;
    private String uploadPath;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private String fileCategory;

    public FileEntity (Long id, String name, String type , Long size, String extension){
        super(id);
        this.name = name;
        this.type = type;
        this.size = size;
        this.extension = extension;
    }

    public FileEntity (){

    }



    public String getOrginalName() {
        return orginalName;
    }

    public void setOrginalName(String orginalName) {
        this.orginalName = orginalName;
    }

    public String getFileCategory() {
        return fileCategory;
    }

    public void setFileCategory(String fileCategory) {
        this.fileCategory = fileCategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getHashId() {
        return hashId;
    }

    public void setHashId(String hashId) {
        this.hashId = hashId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }
}
