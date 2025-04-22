package com.mfilaniu.trustedmediaservice.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtValidateResponse {

    @JsonCreator
    public JwtValidateResponse(@JsonProperty("wallet_address") String walletAddress,
                               @JsonProperty("profile_tag") String profileTag,
                               @JsonProperty("is_valid") Boolean isValid,
                               @JsonProperty("description") String description,
                               @JsonProperty("status_code") Integer statusCode,
                               @JsonProperty("user_id") Long userId) {
        this.walletAddress = walletAddress;
        this.profileTag = profileTag;
        this.isValid = isValid;
        this.description = description;
        this.statusCode = statusCode;
        this.userId = userId;
    }

    @JsonProperty("user_id")
    Long userId;

    @JsonProperty("wallet_address")
    String walletAddress;

    @JsonProperty("profile_tag")
    String profileTag;

    @JsonProperty("is_valid")
    Boolean isValid;

    @JsonProperty("description")
    String description;

    @JsonProperty("status_code")
    Integer statusCode;

    @Override
    public String toString() {
        return "JwtValidateResponse{" +
               "userId=" + userId +
               ", walletAddress='" + walletAddress + '\'' +
               ", profileTag='" + profileTag + '\'' +
               ", isValid=" + isValid +
               ", description='" + description + '\'' +
               ", statusCode=" + statusCode +
               '}';
    }
}
