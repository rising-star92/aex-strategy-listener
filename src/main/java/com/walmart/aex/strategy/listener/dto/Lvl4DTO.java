package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Lvl4DTO {
    private String lvl4Name;
    private Long lvl4Nbr;
    private FinelinePayloadDTO finelines;
}