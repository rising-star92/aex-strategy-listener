package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinelineDTO {

    private Integer finelineId;
    private String finelineName;
    private List<StyleDTO> styles;
}
