package com.walmart.aex.strategy.listener.dto.midas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FinelineRankMetricsDTO {
    private FinelineRankMetricsPayloadDTO payload;
}
