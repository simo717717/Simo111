package com.photo.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.photo.dto.PhotoUploadDto;
import com.photo.entity.Photo;
import com.photo.entity.User;
import com.photo.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final UserService userService;
    private final AIService aiService;

    @Value("${file.upload-dir:#{system.getProperty('user.dir')}+'/uploads'}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            // 确保上传目录存在
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 确保缩略图目录存在
            Path thumbnailPath = Paths.get(uploadDir + "/thumbnails/");
            if (!Files.exists(thumbnailPath)) {
                Files.createDirectories(thumbnailPath);
            }

            log.info("Upload directory initialized: {}", uploadPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to initialize upload directory: ", e);
            throw new RuntimeException("Could not create upload directory: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Photo uploadPhoto(MultipartFile file, PhotoUploadDto uploadDto, String username) throws IOException {
        // 上传目录已经在@PostConstruct中创建，无需重复创建

        // 保存原图
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IOException("File name is empty or null");
        }

        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(originalFilename);

        String filepath = Paths.get(uploadDir, uniqueFilename).toString();
        File destFile = new File(filepath);
        // 确保目标文件的父目录存在
        File parentDir = destFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        file.transferTo(destFile);

        // 生成缩略图
        String thumbnailPath = null;
        try {
            thumbnailPath = generateThumbnail(destFile, uniqueFilename);
        } catch (Exception e) {
            log.warn("无法为文件 {} 生成缩略图: {}", originalFilename, e.getMessage());
            // 缩略图生成失败，继续处理
        }

        // 获取EXIF数据
        String exifData = extractExifData(destFile, fileExtension);

        // 创建Photo实体
        User owner = userService.findByUsername(username);
        Photo photo = new Photo();
        photo.setOriginalName(originalFilename);
        photo.setFilename(uniqueFilename);
        photo.setFilepath(filepath);
        photo.setThumbnailPath(thumbnailPath);
        photo.setMimeType(file.getContentType());
        photo.setFileSize(file.getSize());
        photo.setDescription(uploadDto.getDescription());
        photo.setVisibility(Photo.Visibility.valueOf(uploadDto.getVisibility()));
        photo.setOwner(owner);

        // 处理标签 - 如果启用了AI自动标签，则结合手动标签和AI生成的标签
        List<String> allTags = new ArrayList<>();
        if (uploadDto.getTags() != null) {
            allTags.addAll(uploadDto.getTags());  // 添加手动标签
        }

        if (uploadDto.isAutoTag()) {
            log.info("AI auto-tagging enabled for file: {}", originalFilename);
            // 使用AI服务分析图像内容并生成智能标签
            List<String> aiGeneratedTags = aiService.analyzeImageContent(destFile);
            allTags.addAll(aiGeneratedTags);  // 添加AI生成的标签
            log.info("AI generated {} tags: {}", aiGeneratedTags.size(), aiGeneratedTags);
        } else {
            log.debug("AI auto-tagging disabled for file: {}", originalFilename);
        }

        photo.setTags(allTags);

        // 设置EXIF信息
        if (exifData != null) {
            photo.setExifData(exifData);
        }
        // 总是尝试解析常见的EXIF字段，即使exifData为null（例如对于某些RAW格式）
        parseExifFields(photo, destFile);

        // 设置文件类型
        if (isRawFormat(fileExtension)) {
            photo.setFileType(Photo.FileType.RAW);
        } else {
            photo.setFileType(Photo.FileType.IMAGE);
        }

        // 设置创建时间
        photo.setCreatedAt(LocalDateTime.now());

        return photoRepository.save(photo);
    }

    public Page<Photo> getUserPhotos(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        return photoRepository.findByOwner(user, pageable);
    }

    public Page<Photo> getUserPhotosByVisibility(String username, Photo.Visibility visibility, Pageable pageable) {
        User user = userService.findByUsername(username);
        return photoRepository.findByOwnerAndVisibility(user, visibility, pageable);
    }

    public Optional<Photo> getPhotoById(Long id, String username) {
        Photo photo = photoRepository.findById(id).orElse(null);
        if (photo != null) {
            // 检查权限：用户拥有此照片或照片为公开
            User user = userService.findByUsername(username);
            if (photo.getOwner().getId().equals(user.getId()) || photo.getVisibility() == Photo.Visibility.PUBLIC) {
                return Optional.of(photo);
            }
        }
        return Optional.empty();
    }

    public void deletePhoto(Long id, String username) {
        Photo photo = photoRepository.findById(id).orElse(null);
        if (photo != null) {
            User user = userService.findByUsername(username);
            if (photo.getOwner().getId().equals(user.getId())) {
                // 删除物理文件
                if (photo.getFilepath() != null) {
                    File file = new File(photo.getFilepath());
                    if (file.exists()) {
                        file.delete();
                    }
                }

                if (photo.getThumbnailPath() != null) {
                    File thumbFile = new File(photo.getThumbnailPath());
                    if (thumbFile.exists()) {
                        thumbFile.delete();
                    }
                }

                photoRepository.deleteById(id);
            }
        }
    }

    @Transactional
    public Photo updatePhoto(Long id, PhotoUploadDto updateDto, String username) {
        Photo photo = photoRepository.findById(id).orElse(null);
        if (photo == null) {
            throw new RuntimeException("Photo not found");
        }

        User user = userService.findByUsername(username);
        if (!photo.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this photo");
        }

        // 更新描述
        if (updateDto.getDescription() != null) {
            photo.setDescription(updateDto.getDescription());
        }

        // 更新可见性
        if (updateDto.getVisibility() != null) {
            photo.setVisibility(Photo.Visibility.valueOf(updateDto.getVisibility()));
        }

        // 更新标签
        if (updateDto.getTags() != null) {
            List<String> allTags = new ArrayList<>(updateDto.getTags());

            // 如果启用了AI自动标签，重新生成AI标签
            if (updateDto.isAutoTag()) {
                try {
                    File photoFile = new File(photo.getFilepath());
                    if (photoFile.exists()) {
                        List<String> aiGeneratedTags = aiService.analyzeImageContent(photoFile);
                        allTags.addAll(aiGeneratedTags);
                    }
                } catch (Exception e) {
                    log.warn("Failed to generate AI tags for photo {}: {}", id, e.getMessage());
                }
            }

            photo.setTags(allTags);
        } else if (updateDto.isAutoTag()) {
            // 只有AI标签更新，没有手动标签
            try {
                File photoFile = new File(photo.getFilepath());
                if (photoFile.exists()) {
                    List<String> aiGeneratedTags = aiService.analyzeImageContent(photoFile);
                    List<String> currentTags = photo.getTags() != null ? new ArrayList<>(photo.getTags()) : new ArrayList<>();
                    currentTags.addAll(aiGeneratedTags);
                    photo.setTags(currentTags);
                }
            } catch (Exception e) {
                log.warn("Failed to generate AI tags for photo {}: {}", id, e.getMessage());
            }
        }

        return photoRepository.save(photo);
    }

    private String generateThumbnail(File originalFile, String originalFilename) throws IOException {
        String thumbnailDir = uploadDir + "/thumbnails/";
        Path thumbnailPathObj = Paths.get(thumbnailDir);
        if (!Files.exists(thumbnailPathObj)) {
            Files.createDirectories(thumbnailPathObj);
        }

        String thumbnailFilename = "thumb_" + originalFilename;
        String thumbnailPath = thumbnailDir + thumbnailFilename;

        // 获取文件扩展名
        String fileExtension = getFileExtension(originalFilename);

        // 检查是否为RAW格式
        if (isRawFormat(fileExtension)) {
            log.info("为RAW文件生成缩略图: {} (扩展名: {})", originalFilename, fileExtension);
            // 为RAW文件创建自定义缩略图
            return createRawThumbnail(originalFile, thumbnailPath, fileExtension);
        } else {
            // 使用Thumbnailator生成常规缩略图
            try {
                Thumbnails.of(originalFile)
                        .size(300, 300)
                        .outputQuality(0.8)
                        .toFile(new File(thumbnailPath));
                return thumbnailPath;
            } catch (Exception e) {
                log.warn("无法为文件 {} 生成缩略图，创建默认图标: {}", originalFilename, e.getMessage());
                return createDefaultThumbnail(thumbnailPath, fileExtension);
            }
        }
    }

    private String extractExifData(File file, String fileExtension) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            StringBuilder exifInfo = new StringBuilder();

            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    exifInfo.append(tag.getTagName()).append(": ").append(tag.getDescription()).append("\n");
                }
            }

            return exifInfo.toString();
        } catch (Exception e) {
            // 如果无法读取EXIF数据，记录错误但继续处理
            log.warn("无法读取文件 {} 的EXIF数据: {}", file.getName(), e.getMessage());
            return null;
        }
    }

    private void parseExifFields(Photo photo, File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            for (Directory directory : metadata.getDirectories()) {
                String directoryName = directory.getName();

                // 解析相机型号
                if (directory.containsTag(com.drew.metadata.exif.ExifDirectoryBase.TAG_MAKE) ||
                    directory.containsTag(com.drew.metadata.exif.ExifDirectoryBase.TAG_MODEL)) {
                    String make = directory.getDescription(com.drew.metadata.exif.ExifDirectoryBase.TAG_MAKE);
                    String model = directory.getDescription(com.drew.metadata.exif.ExifDirectoryBase.TAG_MODEL);
                    if (model != null) {
                        photo.setCameraModel(make != null ? make + " " + model : model);
                    }
                }

                // 解析光圈值
                if (directory.containsTag(com.drew.metadata.exif.ExifDirectoryBase.TAG_FNUMBER)) {
                    photo.setAperture(directory.getDescription(com.drew.metadata.exif.ExifDirectoryBase.TAG_FNUMBER));
                } else if (directory.containsTag(com.drew.metadata.exif.ExifDirectoryBase.TAG_APERTURE)) {
                    photo.setAperture(directory.getDescription(com.drew.metadata.exif.ExifDirectoryBase.TAG_APERTURE));
                }

                // 解析快门速度
                if (directory.containsTag(com.drew.metadata.exif.ExifDirectoryBase.TAG_SHUTTER_SPEED)) {
                    photo.setShutterSpeed(directory.getDescription(com.drew.metadata.exif.ExifDirectoryBase.TAG_SHUTTER_SPEED));
                } else if (directory.containsTag(com.drew.metadata.exif.ExifDirectoryBase.TAG_EXPOSURE_TIME)) {
                    photo.setShutterSpeed(directory.getDescription(com.drew.metadata.exif.ExifDirectoryBase.TAG_EXPOSURE_TIME));
                }

                // 解析ISO值
                if (directory.containsTag(com.drew.metadata.exif.ExifDirectoryBase.TAG_ISO_EQUIVALENT)) {
                    photo.setIso(directory.getDescription(com.drew.metadata.exif.ExifDirectoryBase.TAG_ISO_EQUIVALENT));
                }

                // 解析焦距
                if (directory.containsTag(com.drew.metadata.exif.ExifDirectoryBase.TAG_FOCAL_LENGTH)) {
                    String focalLengthStr = directory.getDescription(com.drew.metadata.exif.ExifDirectoryBase.TAG_FOCAL_LENGTH);
                    try {
                        // 尝试解析焦距数字
                        String numericPart = focalLengthStr.replaceAll("[^0-9.]", "");
                        if (!numericPart.isEmpty()) {
                            photo.setFocalLength(Double.parseDouble(numericPart));
                        }
                    } catch (NumberFormatException e) {
                        // 忽略解析错误
                    }
                }
            }
        } catch (Exception e) {
            // 如果无法解析EXIF数据，忽略错误
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    private boolean isRawFormat(String fileExtension) {
        Set<String> rawExtensions = Set.of("cr2", "nef", "arw", "dng", "orf", "rw2", "pef", "sr2", "x3f", "raf");
        return rawExtensions.contains(fileExtension);
    }

    private String generateUniqueFilename(String originalFilename) {
        String fileExtension = getFileExtension(originalFilename);
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String uniqueSuffix = "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

        if (fileExtension.isEmpty()) {
            return baseName + uniqueSuffix;
        }
        return baseName + uniqueSuffix + "." + fileExtension;
    }

    public List<Photo> searchPhotosByTag(String username, String tag) {
        User user = userService.findByUsername(username);
        return photoRepository.findByOwnerAndTag(user, tag, org.springframework.data.domain.PageRequest.of(0, 100)).getContent();
    }

    /**
     * 为RAW格式文件创建缩略图
     * 尝试从RAW文件中提取嵌入式JPEG预览，如果失败则创建默认图标
     */
    private String createRawThumbnail(File originalFile, String thumbnailPath, String fileExtension) throws IOException {
        log.info("开始处理RAW文件缩略图: {}, 文件大小: {} bytes", originalFile.getName(), originalFile.length());

        // 首先尝试使用ImageMagick的convert命令提取RAW预览（如果可用）
        log.info("尝试使用ImageMagick提取RAW预览...");
        if (tryExtractWithImageMagick(originalFile, thumbnailPath)) {
            log.info("成功使用ImageMagick提取RAW预览: {}", originalFile.getName());
            return ensurePngExtension(thumbnailPath);
        }

        // 如果ImageMagick不可用，尝试从元数据中提取嵌入式JPEG预览
        log.info("ImageMagick不可用，尝试从元数据中提取缩略图...");
        try {
            byte[] thumbnailData = extractThumbnailFromMetadata(originalFile);
            if (thumbnailData != null && thumbnailData.length > 0) {
                // 保存缩略图数据到文件
                String outputPath = ensurePngExtension(thumbnailPath);
                File thumbnailFile = new File(outputPath);
                Files.write(thumbnailFile.toPath(), thumbnailData);
                log.info("成功从元数据中提取嵌入式缩略图，文件大小: {} bytes", thumbnailData.length);
                return outputPath;
            } else {
                log.info("未在元数据中找到嵌入式缩略图数据");
            }
        } catch (Exception e) {
            log.info("从元数据中提取缩略图失败: {}", e.getMessage());
        }

        // 如果以上方法都失败，创建默认图标
        log.info("所有方法均失败，创建默认RAW图标");
        return createDefaultThumbnail(thumbnailPath, fileExtension);
    }

    /**
     * 尝试使用ImageMagick提取RAW预览
     */
    private boolean tryExtractWithImageMagick(File originalFile, String thumbnailPath) {
        String outputPath = ensurePngExtension(thumbnailPath);
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");

        log.info("尝试使用ImageMagick提取RAW预览，操作系统: {}, 输出路径: {}", os, outputPath);

        // 尝试不同的ImageMagick命令
        List<String[]> commandsToTry = new ArrayList<>();

        if (isWindows) {
            // Windows: 根据ImageMagick 7+的建议，优先使用"magick"命令
            // 1. 尝试magick命令（ImageMagick 7+推荐方式）
            commandsToTry.add(new String[]{"magick", originalFile.getAbsolutePath() + "[0]", "-thumbnail", "300x300", outputPath});
            // 2. 尝试magick convert（兼容旧语法）
            commandsToTry.add(new String[]{"magick", "convert", originalFile.getAbsolutePath() + "[0]", "-thumbnail", "300x300", outputPath});
            // 3. 尝试convert命令（注意：Windows自带convert命令可能干扰）
            commandsToTry.add(new String[]{"convert", originalFile.getAbsolutePath() + "[0]", "-thumbnail", "300x300", outputPath});

            // 也尝试Windows常见安装路径
            String[] commonPaths = {
                "C:\\Program Files\\ImageMagick\\magick.exe",
                "C:\\Program Files (x86)\\ImageMagick\\magick.exe",
                "C:\\Program Files\\ImageMagick\\convert.exe",
                "C:\\Program Files (x86)\\ImageMagick\\convert.exe",
                // 用户可能安装在默认路径
                System.getenv("ProgramFiles") + "\\ImageMagick\\magick.exe",
                System.getenv("ProgramFiles(x86)") + "\\ImageMagick\\magick.exe"
            };

            for (String fullPath : commonPaths) {
                if (fullPath != null && new File(fullPath).exists()) {
                    log.info("在常见位置找到ImageMagick: {}", fullPath);
                    // 对于magick.exe，尝试两种方式
                    if (fullPath.toLowerCase().contains("magick.exe")) {
                        // 方式1: magick input output
                        commandsToTry.add(0, new String[]{fullPath, originalFile.getAbsolutePath() + "[0]", "-thumbnail", "300x300", outputPath});
                        // 方式2: magick convert input output
                        commandsToTry.add(0, new String[]{fullPath, "convert", originalFile.getAbsolutePath() + "[0]", "-thumbnail", "300x300", outputPath});
                    } else {
                        // convert.exe
                        commandsToTry.add(0, new String[]{fullPath, originalFile.getAbsolutePath() + "[0]", "-thumbnail", "300x300", outputPath});
                    }
                }
            }
        } else {
            // Linux/Mac: 尝试convert命令和magick命令
            commandsToTry.add(new String[]{"magick", originalFile.getAbsolutePath() + "[0]", "-thumbnail", "300x300", outputPath});
            commandsToTry.add(new String[]{"magick", "convert", originalFile.getAbsolutePath() + "[0]", "-thumbnail", "300x300", outputPath});
            commandsToTry.add(new String[]{"convert", originalFile.getAbsolutePath() + "[0]", "-thumbnail", "300x300", outputPath});
        }

        // 也尝试使用dcraw工具（如果可用）
        commandsToTry.add(new String[]{"dcraw", "-e", "-c", originalFile.getAbsolutePath()});

        // 记录要尝试的所有命令
        log.info("将尝试以下命令来提取RAW预览:");
        for (String[] cmd : commandsToTry) {
            log.info("  - {}", String.join(" ", cmd));
        }

        for (String[] command : commandsToTry) {
            try {
                log.info("尝试命令: {}", String.join(" ", command));

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                // 对于dcraw命令，需要特殊处理输出
                boolean isDcraw = command[0].equals("dcraw");
                if (isDcraw) {
                    // dcraw输出到stdout，需要重定向到文件
                    processBuilder.redirectOutput(new File(outputPath));
                    processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
                } else {
                    processBuilder.redirectErrorStream(true);
                }

                Process process = processBuilder.start();

                // 读取输出以便调试
                if (!isDcraw) {
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            log.debug("命令输出: {}", line);
                        }
                    }
                }

                // 等待进程完成，但设置超时
                boolean finished = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
                if (!finished) {
                    process.destroy();
                    log.info("命令超时: {}", command[0]);
                    continue;
                }

                int exitCode = process.exitValue();
                log.info("命令 {} 执行完成，退出码: {}", command[0], exitCode);

                if (exitCode == 0) {
                    File outputFile = new File(outputPath);
                    boolean fileExists = outputFile.exists();
                    long fileSize = fileExists ? outputFile.length() : 0;
                    log.info("输出文件状态 - 存在: {}, 大小: {} bytes", fileExists, fileSize);

                    if (fileExists && fileSize > 0) {
                        log.info("成功使用 {} 提取RAW预览: {}", command[0], originalFile.getName());
                        return true;
                    } else {
                        log.info("输出文件不存在或为空");
                    }
                } else {
                    log.info("命令 {} 失败，退出码: {}", command[0], exitCode);
                }
            } catch (IOException e) {
                String errorMsg = e.getMessage();
                log.info("命令 {} 不存在或无法执行: {}", command[0], errorMsg);

                // 如果是"找不到文件"错误，提供修复建议
                if (errorMsg != null && (errorMsg.contains("系统找不到指定的文件") || errorMsg.contains("CreateProcess error=2") || errorMsg.contains("Cannot run program"))) {
                    log.info("ImageMagick可能未在PATH中配置，或者IDEA需要重启以加载新的环境变量。");
                    log.info("解决方案: 1) 重启IDEA 2) 检查ImageMagick安装 3) 确保PATH包含ImageMagick目录");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("命令 {} 被中断", command[0]);
            } catch (Exception e) {
                log.info("执行命令 {} 时出错: {}", command[0], e.getMessage());
            }
        }

        log.info("所有ImageMagick/dcraw命令尝试均失败");
        log.info("⚠️ 重要: ImageMagick已安装但Java进程无法找到。这通常是因为:");
        log.info("   1. IDEA在ImageMagick安装前启动，需要重启IDEA");
        log.info("   2. PATH环境变量未包含ImageMagick安装目录");
        log.info("   3. ImageMagick未正确添加到系统PATH");
        log.info("解决方案:");
        log.info("   - 重启IDEA (File → Invalidate Caches and Restart)");
        log.info("   - 检查系统PATH: 包含类似 C:\\Program Files\\ImageMagick");
        log.info("   - 或者在IDEA Run Configuration中添加环境变量");
        return false;
    }

    /**
     * 从元数据中提取缩略图
     */
    private byte[] extractThumbnailFromMetadata(File originalFile) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(originalFile);

            // 检查常见的包含缩略图的目录
            for (Directory directory : metadata.getDirectories()) {
                String dirName = directory.getName();

                // 尝试提取缩略图数据 - 使用已知的目录类型
                if (dirName.contains("Exif") || dirName.contains("Thumbnail") || dirName.contains("JPEG")) {
                    for (Tag tag : directory.getTags()) {
                        String tagName = tag.getTagName();

                        // 寻找可能包含缩略图数据的标签
                        if (tagName.contains("Thumbnail") || tagName.contains("Image Data") ||
                            tagName.contains("Preview") || tagName.contains("JPEG")) {
                            try {
                                Object description = tag.getDescription();
                                if (description != null) {
                                    String desc = description.toString();
                                    // 如果描述看起来像包含数据，记录信息
                                    if (desc.length() > 100) {  // 假设缩略图数据会比较大
                                        log.debug("Found potential thumbnail data in tag: {}", tagName);
                                    }
                                }
                            } catch (Exception e) {
                                // 忽略
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error reading metadata for thumbnail extraction: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 确保文件路径以.png结尾
     */
    private String ensurePngExtension(String filePath) {
        if (filePath.toLowerCase().endsWith(".png")) {
            return filePath;
        }
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex > 0) {
            return filePath.substring(0, dotIndex) + ".png";
        }
        return filePath + ".png";
    }

    /**
     * 创建默认缩略图（用于RAW格式或无法生成缩略图的情况）
     * @return 实际保存的缩略图路径
     */
    private String createDefaultThumbnail(String thumbnailPath, String fileExtension) throws IOException {
        int width = 300;
        int height = 300;

        // 创建图像
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 设置背景色 - 为RAW文件使用不同的背景色
        if (isRawFormat(fileExtension)) {
            g2d.setColor(new Color(50, 50, 80)); // 深蓝色背景
        } else {
            g2d.setColor(new Color(240, 240, 240)); // 浅灰色背景
        }
        g2d.fillRect(0, 0, width, height);

        // 设置边框
        if (isRawFormat(fileExtension)) {
            g2d.setColor(new Color(100, 150, 255)); // 亮蓝色边框
        } else {
            g2d.setColor(new Color(200, 200, 200)); // 灰色边框
        }
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(10, 10, width - 20, height - 20);

        // 设置文本
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        // 显示文件类型
        String fileTypeText;
        if (isRawFormat(fileExtension)) {
            fileTypeText = "RAW";
            g2d.setColor(Color.WHITE); // 白色文字在深色背景上
        } else {
            fileTypeText = fileExtension.toUpperCase();
            g2d.setColor(new Color(100, 100, 100));
        }

        // 绘制文本居中
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(fileTypeText);
        int textHeight = fm.getHeight();
        int x = (width - textWidth) / 2;
        int y = (height - textHeight) / 2 + fm.getAscent();

        g2d.drawString(fileTypeText, x, y);

        // 绘制图标
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        if (isRawFormat(fileExtension)) {
            g2d.setColor(new Color(220, 220, 220)); // 浅灰色文字
        } else {
            g2d.setColor(new Color(150, 150, 150));
        }
        String iconText = isRawFormat(fileExtension) ? "专业格式" : "图像文件";
        FontMetrics fm2 = g2d.getFontMetrics();
        int iconTextWidth = fm2.stringWidth(iconText);
        int iconX = (width - iconTextWidth) / 2;
        int iconY = y + 30;
        g2d.drawString(iconText, iconX, iconY);

        g2d.dispose();

        // 确定输出路径
        String outputPath = thumbnailPath;
        if (!thumbnailPath.toLowerCase().endsWith(".png")) {
            outputPath = thumbnailPath.substring(0, thumbnailPath.lastIndexOf('.')) + ".png";
        }

        // 保存图像为PNG
        File thumbnailFile = new File(outputPath);
        ImageIO.write(image, "PNG", thumbnailFile);

        return outputPath;
    }
    public File getPhotoFileForDownload(Long id, String username) {
        Optional<Photo> photoOpt = getPhotoById(id, username);
        if (photoOpt.isEmpty()) {
            return null; // 无权限或照片不存在
        }

        Photo photo = photoOpt.get();
        String filepath = photo.getFilepath();
        if (filepath == null || filepath.isEmpty()) {
            return null;
        }

        File file = new File(filepath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        return file;
    }
}