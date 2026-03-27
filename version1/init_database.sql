-- 照片管理系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS photo_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE photo_management;

-- 如果表已存在，先删除（可选）
DROP TABLE IF EXISTS photo_tags;
DROP TABLE IF EXISTS photos;
DROP TABLE IF EXISTS users;

-- 创建用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    bio TEXT,
    avatar_url VARCHAR(500),
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建照片表
CREATE TABLE photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    filepath VARCHAR(500) NOT NULL,
    thumbnail_path VARCHAR(500),
    original_name VARCHAR(255),
    mime_type VARCHAR(100),
    file_size BIGINT,
    description TEXT,
    visibility ENUM('PUBLIC', 'PRIVATE', 'FRIENDS') DEFAULT 'PRIVATE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    exif_data TEXT,
    camera_model VARCHAR(100),
    aperture VARCHAR(20),
    shutter_speed VARCHAR(20),
    iso VARCHAR(10),
    focal_length DECIMAL(10, 2),
    location VARCHAR(255),
    file_type ENUM('IMAGE', 'RAW') DEFAULT 'IMAGE',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建照片标签关联表
CREATE TABLE photo_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    photo_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (photo_id) REFERENCES photos(id) ON DELETE CASCADE,
    INDEX idx_photo_tag (photo_id, tag)
);

-- 创建索引以提高查询性能
CREATE INDEX idx_photos_user_id ON photos(user_id);
CREATE INDEX idx_photos_visibility ON photos(visibility);
CREATE INDEX idx_photos_created_at ON photos(created_at);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);