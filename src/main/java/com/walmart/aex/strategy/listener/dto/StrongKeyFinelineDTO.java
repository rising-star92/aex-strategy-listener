package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrongKeyFinelineDTO {
    private Long finelineId;
    private String finelineName;
    private List<StrongKeyStyleDTO> styles;
}
