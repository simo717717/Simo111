package com.photo.repository;

import com.photo.entity.Photo;
import com.photo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Page<Photo> findByOwnerAndVisibility(User owner, Photo.Visibility visibility, Pageable pageable);

    Page<Photo> findByOwner(User owner, Pageable pageable);

    List<Photo> findByOwnerId(Long ownerId);

    Page<Photo> findByOwnerIdAndVisibility(Long ownerId, Photo.Visibility visibility, Pageable pageable);

    @Query("SELECT p FROM Photo p WHERE p.owner = :owner AND :tag MEMBER OF p.tags")
    Page<Photo> findByOwnerAndTag(@Param("owner") User owner, @Param("tag") String tag, Pageable pageable);

    @Query("SELECT p FROM Photo p WHERE p.visibility = 'PUBLIC' OR p.owner = :owner")
    Page<Photo> findPublicAndUserPhotos(@Param("owner") User owner, Pageable pageable);

    long countByOwnerId(Long ownerId);
}