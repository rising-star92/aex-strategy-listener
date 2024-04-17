package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Lvl2DTO {
    private Long lvl2Nbr;
    private String lvl2Desc;
    private List<Lvl3DTO> lvl3List;
}
