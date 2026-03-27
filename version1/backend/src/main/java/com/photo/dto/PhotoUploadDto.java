package com.photo.dto;

import lombok.Data;

import java.util.List;

@Data
public class PhotoUploadDto {
    private String description;
    private List<String> tags;
    private String visibility; // PUBLIC, PRIVATE, FRIENDS
    private boolean autoTag = false;  // 是否启用AI智能标签识别
}