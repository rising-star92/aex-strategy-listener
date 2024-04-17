package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StrategyLvl1DTO {
    private Long lvl1Nbr;
    private String lvl1Name;
    private List<StrategyLvl2DTO> lvl2List;
}