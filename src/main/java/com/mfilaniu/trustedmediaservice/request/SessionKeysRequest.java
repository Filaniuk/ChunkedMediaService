package com.mfilaniu.trustedmediaservice.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class SessionKeysRequest {

    @JsonProperty("media_ids_and_keys")
    private Map<String, String> mediaIdsAndKeys;

}