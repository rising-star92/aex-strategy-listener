package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.walmart.aex.strategy.listener.dto.DateDTO;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WeatherClusterDTO {
    private WeatherClusterTypeDTO type;
    private Integer storeCount;
    private Integer sellingWeeks;
    private Boolean inStoreDisabledInLP;
    private Boolean markDownDisabledInLP;
    private DateDTO inStoreDate;
    private DateDTO markDownDate;
    private Double lySales;
    private Long lyUnits;
    private Long onHandQty;
    private Double salesToStockRatio;
    private Double forecastedSales;
    private Long forecastedUnits;
    private Integer algoClusterRanking;
}
