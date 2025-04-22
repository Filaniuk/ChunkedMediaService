package com.mfilaniu.trustedmediaservice.repository;

import com.mfilaniu.trustedmediaservice.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Optional<Media> findByMediaId(UUID mediaId);

}
