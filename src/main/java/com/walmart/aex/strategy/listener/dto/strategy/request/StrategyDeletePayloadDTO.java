package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategyDeletePayloadDTO {
    private StrategyStrongKeyDTO strongKey;
    private StrategyPayloadDTO planStrategyDto;
}
