package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OnlineMetricsDTO {
    private ProductAttributesDTO productAttributes;
    private FinancialAttributesDTO financialAttributes;
}
