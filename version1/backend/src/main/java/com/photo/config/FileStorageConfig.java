package com.photo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {

    @Value("${file.upload-dir:#{system.getProperty('user.dir')}+'/uploads'}")
    private String uploadDir;

    private static String staticUploadDir;

    @PostConstruct
    public void init() {
        staticUploadDir = uploadDir;
        System.out.println("DEBUG FileStorageConfig initialized upload directory: " + staticUploadDir);
    }

    public static String getUploadDir() {
        return staticUploadDir;
    }

    public static Path getUploadDirPath() {
        return Paths.get(staticUploadDir).toAbsolutePath().normalize();
    }
}