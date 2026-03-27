package com.photo.dto;

import com.photo.entity.Photo;
import com.photo.config.FileStorageConfig;
import lombok.Data;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PhotoResponseDto {
    private Long id;
    private String filename;
    private String filepath;
    private String thumbnailPath;
    private String fileUrl;           // 新增：文件URL
    private String thumbnailUrl;      // 新增：缩略图URL
    private String originalName;
    private String mimeType;
    private Long fileSize;
    private String description;
    private Photo.Visibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String ownerUsername;
    private List<String> tags;
    private String exifData;
    private String cameraModel;
    private String aperture;
    private String shutterSpeed;
    private String iso;
    private Double focalLength;
    private String location;
    private Photo.FileType fileType;

    public PhotoResponseDto(Photo photo) {
        this.id = photo.getId();
        this.filename = photo.getFilename();
        this.filepath = photo.getFilepath();
        this.thumbnailPath = photo.getThumbnailPath();
        this.originalName = photo.getOriginalName();
        this.mimeType = photo.getMimeType();
        this.fileSize = photo.getFileSize();
        this.description = photo.getDescription();
        this.visibility = photo.getVisibility();
        this.createdAt = photo.getCreatedAt();
        this.updatedAt = photo.getUpdatedAt();
        this.ownerUsername = photo.getOwner().getUsername();
        this.tags = photo.getTags();
        this.exifData = photo.getExifData();
        this.cameraModel = photo.getCameraModel();
        this.aperture = photo.getAperture();
        this.shutterSpeed = photo.getShutterSpeed();
        this.iso = photo.getIso();
        this.focalLength = photo.getFocalLength();
        this.location = photo.getLocation();
        this.fileType = photo.getFileType();

        // 转换文件路径为URL
        this.fileUrl = convertPathToUrl(photo.getFilepath(), "file");
        this.thumbnailUrl = convertPathToUrl(photo.getThumbnailPath(), "thumbnail");
    }

    /**
     * 将文件系统路径转换为URL
     * 例如: C:/Users/Simo/IdeaProjects/version1/uploads/filename.jpg -> /uploads/filename.jpg
     */
    private String convertPathToUrl(String filepath, String type) {
        if (filepath == null || filepath.isEmpty()) {
            return null;
        }

        // 使用配置的上传目录，如果不可用则回退到动态构建
        String uploadDir = FileStorageConfig.getUploadDir();
        boolean usingFallback = false;
        String sourceInfo = "configured";

        if (uploadDir == null) {
            // 回退到动态获取上传目录 - 使用与PhotoService相同的逻辑
            usingFallback = true;
            sourceInfo = "fallback (user.dir based)";
            String userDir = System.getProperty("user.dir");
            uploadDir = userDir + File.separator + "uploads";
        }

        try {
            // 使用Path对象规范化路径
            Path uploadDirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = Paths.get(filepath).toAbsolutePath().normalize();

            // 调试日志
            System.out.println("DEBUG " + type + " URL conversion:");
            System.out.println("  Source: " + sourceInfo);
            System.out.println("  uploadDir: " + uploadDir);
            System.out.println("  uploadDirPath (normalized): " + uploadDirPath);
            System.out.println("  filepath: " + filepath);
            System.out.println("  filePath (normalized): " + filePath);
            System.out.println("  startsWith: " + filePath.startsWith(uploadDirPath));

            if (filePath.startsWith(uploadDirPath)) {
                // 获取相对路径
                Path relativePath = uploadDirPath.relativize(filePath);
                // 构建URL路径
                String urlPath = "/uploads/" + relativePath.toString().replace(File.separatorChar, '/');
                // 确保没有双斜杠
                urlPath = urlPath.replace("//", "/");
                System.out.println("  Generated URL: " + urlPath);
                return urlPath;
            } else {
                // 如果路径不符合预期，返回null
                System.out.println("  ERROR: File path is not under upload directory");
                System.out.println("  Upload dir: " + uploadDirPath);
                System.out.println("  File path: " + filePath);
                return null;
            }
        } catch (Exception e) {
            System.out.println("  ERROR during URL conversion: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}