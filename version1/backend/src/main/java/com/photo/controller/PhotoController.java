package com.photo.controller;

import com.photo.dto.PhotoUploadDto;
import com.photo.dto.PhotoResponseDto;
import com.photo.entity.Photo;
import com.photo.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.io.File;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PhotoController {

    private final PhotoService photoService;
    private final com.photo.config.JwtService jwtService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "visibility", defaultValue = "PRIVATE") String visibility,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "autoTag", defaultValue = "false") boolean autoTag,
            @RequestHeader("Authorization") String token) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            String username = extractUsernameFromToken(token);

            PhotoUploadDto uploadDto = new PhotoUploadDto();
            uploadDto.setDescription(description);
            uploadDto.setTags(tags);
            uploadDto.setVisibility(visibility);
            uploadDto.setAutoTag(autoTag);

            Photo photo = photoService.uploadPhoto(file, uploadDto, username);

            PhotoResponseDto responseDto = new PhotoResponseDto(photo);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not upload file: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<PhotoResponseDto>> getUserPhotos(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String username = extractUsernameFromToken(token);

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Photo> photosPage = photoService.getUserPhotos(username, pageable);
        Page<PhotoResponseDto> responsePage = photosPage.map(PhotoResponseDto::new);

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponseDto> getPhotoById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String username = extractUsernameFromToken(token);
        return photoService.getPhotoById(id, username)
                .map(photo -> ResponseEntity.ok(new PhotoResponseDto(photo)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePhoto(
            @PathVariable Long id,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "visibility", required = false) String visibility,
            @RequestParam(value = "tags", required = false) String tagsStr,
            @RequestParam(value = "autoTag", defaultValue = "false") boolean autoTag,
            @RequestHeader("Authorization") String token) {

        try {
            String username = extractUsernameFromToken(token);

            PhotoUploadDto updateDto = new PhotoUploadDto();
            updateDto.setDescription(description);
            updateDto.setVisibility(visibility);
            updateDto.setAutoTag(autoTag);

            // 解析标签字符串
            if (tagsStr != null && !tagsStr.trim().isEmpty()) {
                List<String> tags = Arrays.stream(tagsStr.split(","))
                        .map(String::trim)
                        .filter(tag -> !tag.isEmpty())
                        .collect(Collectors.toList());
                updateDto.setTags(tags);
            }

            Photo updatedPhoto = photoService.updatePhoto(id, updateDto, username);
            PhotoResponseDto responseDto = new PhotoResponseDto(updatedPhoto);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Photo not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().equals("Unauthorized to update this photo")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to update this photo");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not update photo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not update photo: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<PhotoResponseDto>> searchPhotosByTag(
            @RequestParam String tag,
            @RequestHeader("Authorization") String token) {

        String username = extractUsernameFromToken(token);
        List<Photo> photos = photoService.searchPhotosByTag(username, tag);
        List<PhotoResponseDto> responseDtos = photos.stream()
                .map(PhotoResponseDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/visibility/{visibility}")
    public ResponseEntity<Page<PhotoResponseDto>> getPhotosByVisibility(
            @PathVariable String visibility,
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = extractUsernameFromToken(token);
        Pageable pageable = PageRequest.of(page, size);

        Page<Photo> photosPage = photoService.getUserPhotosByVisibility(
                username,
                Photo.Visibility.valueOf(visibility.toUpperCase()),
                pageable
        );

        Page<PhotoResponseDto> responsePage = photosPage.map(PhotoResponseDto::new);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadPhoto(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String username = extractUsernameFromToken(token);
        File photoFile = photoService.getPhotoFileForDownload(id, username);

        if (photoFile == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(photoFile.toPath().toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // 获取原始文件名
            Photo photo = photoService.getPhotoById(id, username)
                    .orElseThrow(() -> new RuntimeException("Photo not found"));
            String originalName = photo.getOriginalName();

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                       "attachment; filename=\"" + originalName + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            // 设置Content-Type
            String contentType = photo.getMimeType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(photoFile.length())
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePhoto(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        try {
            String username = extractUsernameFromToken(token);
            photoService.deletePhoto(id, username);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Photo not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().equals("Unauthorized to delete this photo")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to delete this photo");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not delete photo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not delete photo: " + e.getMessage());
        }
    }

    // 辅助方法：从JWT令牌中提取用户名
    private String extractUsernameFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            return jwtService.extractUsername(jwtToken);
        }
        return null;
    }
}