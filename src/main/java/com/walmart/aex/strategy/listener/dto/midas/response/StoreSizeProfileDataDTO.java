package com.walmart.aex.strategy.listener.dto.midas.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StoreSizeProfileDataDTO {
    private StoreSizeProfilePayloadDTO payload;
}
