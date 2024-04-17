package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategyStrongKeyDTO {
    private Long planId;
    private String planDesc;
    private Integer lvl0Nbr;
    private String lvl0GenDesc1;
    private Integer lvl1Nbr;
    private String lvl1GenDesc1;
    private Integer lvl2Nbr;
    private String lvl2GenDesc1;
    private Integer lvl3Nbr;
    private Integer lvl4Nbr;
    private StrategyFinelinesDTO fineline;
}
