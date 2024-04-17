package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StrategyLvl3DTO {

    private String lvl3Name;
    private Long lvl3Nbr;
    private StrategyDTO strategy;
    private List<StrategyLvl4DTO> lvl4List;
}
