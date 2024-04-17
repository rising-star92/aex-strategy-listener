package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Lvl1DTO {
    private Long lvl1Nbr;
    private String lvl1Desc;
    private List<Lvl2DTO> lvl2List;
}
