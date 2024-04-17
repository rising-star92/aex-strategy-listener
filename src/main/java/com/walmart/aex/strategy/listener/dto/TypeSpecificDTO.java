package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TypeSpecificDTO {
    private String traitChoice;
    private String preferredFixtureType;
    private String preferredFixtureType2;
    private String preferredFixtureType3;
    private String preferredFixtureType4;
    private String outFitting;
    private Boolean isTransactableStartWkEditedInAP;
    private Boolean isTransactableEndWkEditedInAP;
    private List<ColorsDTO> colors;
    private List<BrandDto> brands;
    private ProductDimensionsDTO productDimensions;
}
