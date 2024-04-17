package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StrategyLvl2DTO {
    private Long lvl2Nbr;
    private String lvl2Name;
    private List<StrategyLvl3DTO> lvl3List;
}
