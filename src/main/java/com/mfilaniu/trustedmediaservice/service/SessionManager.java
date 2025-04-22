package com.mfilaniu.trustedmediaservice.service;

import com.mfilaniu.trustedmediaservice.request.SessionKeysRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.logging.Logger;

@Service
public class SessionManager {

    private static final Logger LOG = Logger.getLogger(SessionManager.class.getName());

    public ResponseEntity<?> storeKeysInSession(HttpSession session,
                                                SessionKeysRequest keyRequest,
                                                String token) {

        for (Map.Entry<String, String> entry : keyRequest.getMediaIdsAndKeys().entrySet()) {
            session.setAttribute(entry.getKey(), entry.getValue());
        }

        return ResponseEntity.ok("Keys stored in session");
    }

}