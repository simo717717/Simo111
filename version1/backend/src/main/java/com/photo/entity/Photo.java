package com.photo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "photos")
@Data
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String filepath;

    @Column
    private String thumbnailPath;

    @Column
    private String originalName;

    @Column
    private String mimeType;

    @Column
    private Long fileSize;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PRIVATE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User owner;

    @ElementCollection
    @CollectionTable(name = "photo_tags", joinColumns = @JoinColumn(name = "photo_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Column(columnDefinition = "TEXT")
    private String exifData;

    @Column
    private String cameraModel;

    @Column
    private String aperture;

    @Column
    private String shutterSpeed;

    @Column
    private String iso;

    @Column
    private Double focalLength;

    @Column
    private String location;

    @Enumerated(EnumType.STRING)
    private FileType fileType = FileType.IMAGE;

    public enum Visibility {
        PUBLIC, PRIVATE, FRIENDS
    }

    public enum FileType {
        IMAGE, RAW
    }
}