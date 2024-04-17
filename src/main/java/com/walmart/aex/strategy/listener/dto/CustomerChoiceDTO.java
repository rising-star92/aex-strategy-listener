package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CustomerChoiceDTO {
    private String ccId;
    private String altCcDesc;
    private MetricsDTO metrics;
    private String channel;
    private String baseItemId;
    private Long finelineId;
}