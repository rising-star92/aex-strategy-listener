package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrongKeyDTO {
    private Long planId;
    private Integer lvl0Nbr;
    private String lvl0GenDesc1;
    private Integer lvl1Nbr;
    private String lvl1GenDesc1;
    private Integer lvl2Nbr;
    private String lvl2GenDesc1;
    private Integer lvl3Nbr;
    private Integer lvl4Nbr;
    private StrongKeyFinelineDTO fineline;
}
