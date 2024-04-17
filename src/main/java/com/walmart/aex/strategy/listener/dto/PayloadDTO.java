package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PayloadDTO {

    private Long planId;
    private String planDesc;
    private List<MerchantDetailsDTO> merchant;
    private Long lvl0Nbr;
    private Long lvl1Nbr;
    private Long lvl2Nbr;
    private Long lvl3Nbr;
    private Long lvl4Nbr;
    private Lvl3DTO lvl3;
}
