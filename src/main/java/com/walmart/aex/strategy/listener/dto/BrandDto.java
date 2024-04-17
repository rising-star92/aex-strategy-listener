package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BrandDto {
    private Integer brandId;
    private String brandLabelCode;
    private String brandName;
    private String brandType;
}
