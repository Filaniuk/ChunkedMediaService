package com.mfilaniu.trustedmediaservice.service;

import com.mfilaniu.trustedmediaservice.entity.Media;
import com.mfilaniu.trustedmediaservice.repository.MediaRepository;
import com.mfilaniu.trustedmediaservice.util.AESDecryptor;
import com.mfilaniu.trustedmediaservice.util.CryptoUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.crypto.SecretKey;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class MediaDownloadService {

    private static final int CHUNK_SIZE = 1024 * 1024;
    private final MediaRepository mediaRepository;
    private final AWSService awsService;
    private static final Logger LOG = Logger.getLogger(MediaDownloadService.class.getName());

    public ResponseEntity<StreamingResponseBody> streamMediaForDownload(UUID mediaId, HttpSession session) {
        Media media = mediaRepository.findByMediaId(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found: " + mediaId));

        String key = (String) session.getAttribute(mediaId.toString());
        SecretKey secretKey = AESDecryptor.decodeKey(key);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(media.getMediaType()));
        headers.setContentDisposition(ContentDisposition.attachment().filename(media.getOriginalFilename()).build());
        headers.set("Accept-Ranges", "bytes");

        headers.setContentLength(media.getSizeBytes());

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream -> {
                    try {
                        streamAllChunks(media, secretKey, outputStream);
                    } catch (Exception e) {
                        LOG.severe("Error streaming media: " + e.getMessage());
                    }
                });

    }

    private void streamAllChunks(Media media, SecretKey secretKey, OutputStream outputStream) throws Exception {
        long size = media.getSizeBytes();
        int totalChunks = (int) Math.ceil((double) size / CHUNK_SIZE);

        for (int i = 0; i < totalChunks; i++) {
            String s3Key = "media/" + media.getMessageUUID() + "/" + media.getMediaId() + "/chunk" + i;
            byte[] encrypted = awsService.download(s3Key);
            byte[] iv = CryptoUtil.deriveChunkIV(media.getMessageUUID(), i);
            byte[] decrypted = AESDecryptor.decryptChunk(encrypted, secretKey, iv);

            // Last chunk might need trimming
            int length = decrypted.length;
            if (i == totalChunks - 1) {
                long remaining = size - (long) i * CHUNK_SIZE;
                length = (int) remaining;
            }

            outputStream.write(decrypted, 0, length);
            outputStream.flush();
        }
    }
}