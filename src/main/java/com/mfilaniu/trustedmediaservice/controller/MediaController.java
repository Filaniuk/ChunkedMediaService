package com.mfilaniu.trustedmediaservice.controller;

import com.mfilaniu.trustedmediaservice.request.SessionKeysRequest;
import com.mfilaniu.trustedmediaservice.service.MediaDownloadService;
import com.mfilaniu.trustedmediaservice.service.MediaStreamService;
import com.mfilaniu.trustedmediaservice.util.AuthClient;
import jakarta.servlet.http.HttpSession;
import com.mfilaniu.trustedmediaservice.response.JwtValidateResponse;
import com.mfilaniu.trustedmediaservice.service.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/media")
public class MediaController {

    private final MediaStreamService mediaStreamService;
    private final MediaDownloadService downloadService;
    private final SessionManager sessionManager;
    private static Logger LOG = Logger.getLogger(MediaController.class.getName());

    @Autowired
    public MediaController(MediaStreamService mediaStreamService,
                           MediaDownloadService downloadService,
                           SessionManager sessionManager) {
        this.mediaStreamService = mediaStreamService;
        this.downloadService = downloadService;
        this.sessionManager = sessionManager;
    }

    @GetMapping("/stream/proxy/{mediaId}")
    public ResponseEntity<?> streamProxy(
            HttpSession session,
            @PathVariable UUID mediaId,
            @CookieValue(value = "jwt_token", required = false) String token,
            @RequestHeader(value = "Range", required = false) String range
    ) {
        LOG.info("Media ID: " + mediaId);
        LOG.info("Range header: " + range);
        LOG.info("Token: " + token);

        JwtValidateResponse jwtValidateResponse = AuthClient.validateToken(token);
        if (jwtValidateResponse == null || !jwtValidateResponse.getIsValid()) {
            LOG.info("Invalid token: " + token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return mediaStreamService.streamMediaByRange(mediaId, range, session);
    }

    @GetMapping("/download/{mediaId}")
    public ResponseEntity<StreamingResponseBody> downloadMedia(
            HttpSession session,
            @PathVariable UUID mediaId,
            @CookieValue(value = "jwt_token", required = false) String token) {
        LOG.info("Media ID: " + mediaId);
        LOG.info("Token: " + token);
        JwtValidateResponse jwtValidateResponse = AuthClient.validateToken(token);
        if (jwtValidateResponse == null || !jwtValidateResponse.getIsValid()) {
            LOG.info("Invalid token: " + token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return downloadService.streamMediaForDownload(mediaId, session);
    }

    @PostMapping("/session/keys")
    public ResponseEntity<?> storeKeysInSession(
            HttpSession userSession,
            @RequestBody SessionKeysRequest sessionKeysRequest,
            @CookieValue(value = "jwt_token", required = false) String token) {
        return sessionManager.storeKeysInSession(userSession, sessionKeysRequest, token);
    }

}
