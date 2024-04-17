package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.walmart.aex.strategy.listener.dto.SizeCluster;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StrategyDTO {
    private List<WeatherClusterDTO> weatherClusters;
    private List<FixtureDTO> fixture;
    private List<PresentationUnitDTO> presentationUnits;
    private List<SizeCluster> storeSizeClusters;
    private List<SizeCluster> onlineSizeClusters;
}
