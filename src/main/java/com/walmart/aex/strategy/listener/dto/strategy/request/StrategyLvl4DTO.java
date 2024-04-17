package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StrategyLvl4DTO {
    private String lvl4Name;
    private Long lvl4Nbr;
    private String channel;
    private List<StrategyFinelinesDTO> finelines;
}