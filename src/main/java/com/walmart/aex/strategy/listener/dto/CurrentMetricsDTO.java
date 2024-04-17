package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CurrentMetricsDTO {
    private String planComments;
    private StoreMetricsDTO store;
    private OnlineMetricsDTO online;
    private OmniMetricsDTO omni;
}
