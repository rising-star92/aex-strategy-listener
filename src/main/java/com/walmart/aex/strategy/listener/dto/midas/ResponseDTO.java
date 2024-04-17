package com.walmart.aex.strategy.listener.dto.midas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ResponseDTO {
    private Integer analyticsClusterId;
    private Long finelineNbr;
    private Double salesAmtLastYr;
    private Long salesUnitsLastYr;
    private Long forecastedDemandUnits;
    private Double forecastedDemandSales;
    private Long onHandQty;
    private Double salesToStockRatio;
    private Integer rank;
}
