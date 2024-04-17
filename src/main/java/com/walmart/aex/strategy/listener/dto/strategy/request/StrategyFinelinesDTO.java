package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.walmart.aex.strategy.listener.dto.BrandDto;
import com.walmart.aex.strategy.listener.dto.ProductDimensionsDTO;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StrategyFinelinesDTO {
    private Long finelineNbr;
    private String finelineName;
    private String altFinelineName;
    private String traitChoice;
    private String outFitting;
    private String channel;
    private List<StrategyStyleDTO> styles;
    private StrategyDTO strategy;
    private List<BrandDto> brands;
    private ProductDimensionsDTO productDimensions;
}
