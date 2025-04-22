package com.mfilaniu.trustedmediaservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "media", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"message_uuid", "media_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"message"})
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "media_id", nullable = false, columnDefinition = "UUID")
    private UUID mediaId;

    @Column(name = "original_filename", nullable = false, columnDefinition = "TEXT")
    private String originalFilename;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(name = "message_uuid", nullable = false, columnDefinition = "UUID")
    private UUID messageUUID;


}
