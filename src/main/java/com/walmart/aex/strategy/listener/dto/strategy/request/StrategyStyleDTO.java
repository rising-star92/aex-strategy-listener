package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StrategyStyleDTO {
    private String styleNbr;
    private String altStyleDesc;
    private String channel;
    private List<StrategyCustomerChoiceDTO> customerChoices;
    private String baseItemId;
    private List<String> ccIds;
    private StrategyDTO strategy;

}
