package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StrategyPayloadDTO {
    private Long planId;
    private String planDesc;
    private Long lvl0Nbr;
    private String lvl0Name;
    private List<StrategyLvl1DTO> lvl1List;
}
