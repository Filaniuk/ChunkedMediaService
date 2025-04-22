package com.mfilaniu.trustedmediaservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mfilaniu.trustedmediaservice.response.JwtValidateResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.logging.Logger;

public class AuthClient {

    private static Logger LOG = Logger.getLogger(AuthClient.class.getName());

    //validating token
    public static JwtValidateResponse validateToken(String token) {
        LOG.info("Validating token: " + token);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:5631/auth/jwt/validate/tag")
                .addHeader("Authorization", token)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (response.code() == 200) {
            JwtValidateResponse jwtValidateResponse = null;
            try {
                String responseString = response.body().string();
                jwtValidateResponse = objectMapper.readValue(responseString, JwtValidateResponse.class);
            } catch (IOException e) {
                LOG.severe("Failed to parse response: " + e.getMessage());
            }
            return jwtValidateResponse;
        }

        return null;
    }

}
