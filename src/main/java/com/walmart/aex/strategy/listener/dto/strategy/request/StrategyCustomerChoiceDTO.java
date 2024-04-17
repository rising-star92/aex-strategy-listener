package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StrategyCustomerChoiceDTO {
    private String ccId;
    private String altCcDesc;
    private String colorName;
    private String colorFamily;
    private String channel;
    private StrategyDTO strategy;
}
