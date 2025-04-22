package com.mfilaniu.trustedmediaservice.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class CryptoUtil {

    public static byte[] deriveChunkIV(UUID messageUuid, int chunkIndex) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(messageUuid.toString().getBytes(StandardCharsets.UTF_8));
        digest.update(ByteBuffer.allocate(4).putInt(chunkIndex).array()); // Big-endian
        byte[] hash = digest.digest();
        return Arrays.copyOf(hash, 16); // 128-bit IV
    }

}