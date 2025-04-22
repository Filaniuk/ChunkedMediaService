package com.mfilaniu.trustedmediaservice.service;

import com.mfilaniu.trustedmediaservice.entity.Media;
import com.mfilaniu.trustedmediaservice.repository.MediaRepository;
import com.mfilaniu.trustedmediaservice.util.AESDecryptor;
import com.mfilaniu.trustedmediaservice.util.CryptoUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaStreamService {

    private static final int CHUNK_SIZE = 1024 * 1024;
    private static final long DEFAULT_RANGE_SIZE = 1024 * 1024;
    private final MediaRepository mediaRepository;
    private final AWSService awsService;

    public ResponseEntity<Resource> streamMediaByRange(
            UUID mediaId,
            String rangeHeader,
            HttpSession session
    ) {
        Media media = mediaRepository.findByMediaId(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found: " + mediaId));

        String key = (String) session.getAttribute(mediaId.toString());
        SecretKey secretKey = AESDecryptor.decodeKey(key);

        String mediaType = media.getMediaType();
        long totalSize = media.getSizeBytes();

        if (mediaType.startsWith("image/")) {
            return buildFullImageResponse(media, secretKey);
        }

        return buildPartialOrFullResponse(
                media,
                secretKey,
                totalSize,
                rangeHeader,
                mediaType.startsWith("audio/") || mediaType.startsWith("video/")
        );
    }

    private ResponseEntity<Resource> buildFullImageResponse(Media media, SecretKey secretKey) {
        try {
            byte[] fullData = getDecryptedBytesForRange(media, secretKey, 0, media.getSizeBytes() - 1);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(media.getMediaType()));
            headers.setContentLength(fullData.length);
            headers.set("Accept-Ranges", "bytes");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(new ByteArrayInputStream(fullData)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to stream image", e);
        }
    }

    private ResponseEntity<Resource> buildPartialOrFullResponse(
            Media media, SecretKey secretKey, long totalSize,
            String rangeHeader, boolean allowPartial) {

        long start = 0;
        long end = totalSize - 1;
        boolean isRangeRequested = false;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] parts = rangeHeader.replace("bytes=", "").split("-");
            try {
                start = Long.parseLong(parts[0]);
                isRangeRequested = true;
            } catch (NumberFormatException ignored) {
            }

            if (parts.length > 1 && !parts[1].isBlank()) {
                try {
                    end = Long.parseLong(parts[1]);
                } catch (NumberFormatException ignored) {
                }
            } else {
                end = Math.min(start + DEFAULT_RANGE_SIZE - 1, totalSize - 1);
            }
        }

        try {
            byte[] data = getDecryptedBytesForRange(media, secretKey, start, end);
            long contentLength = end - start + 1;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept-Ranges", "bytes");
            headers.setContentLength(contentLength);
            headers.setContentType(MediaType.parseMediaType(media.getMediaType()));


            if (isRangeRequested) {
                headers.set("Content-Range", "bytes " + start + "-" + end + "/" + totalSize);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(new InputStreamResource(new ByteArrayInputStream(data)));
            } else {
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(new InputStreamResource(new ByteArrayInputStream(data)));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to stream media", e);
        }
    }

    private byte[] getDecryptedBytesForRange(Media media, SecretKey secretKey, long start, long end) throws Exception {
        int startChunk = (int) (start / CHUNK_SIZE);
        int endChunk = (int) (end / CHUNK_SIZE);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        for (int i = startChunk; i <= endChunk; i++) {
            String s3Key = "media/" + media.getMessageUUID() + "/" + media.getMediaId() + "/chunk" + i;
            byte[] encrypted = awsService.download(s3Key);
            byte[] iv = CryptoUtil.deriveChunkIV(media.getMessageUUID(), i);
            byte[] decrypted = AESDecryptor.decryptChunk(encrypted, secretKey, iv);
            buffer.write(decrypted);
        }

        byte[] full = buffer.toByteArray();
        int offset = (int) (start % CHUNK_SIZE);
        int length = (int) (end - start + 1);
        return Arrays.copyOfRange(full, offset, offset + length);
    }
}