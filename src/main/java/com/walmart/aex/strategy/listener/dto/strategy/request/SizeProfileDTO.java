package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class SizeProfileDTO {
    private Integer ahsSizeId;
    private String sizeDesc;
    private Double sizeProfilePrcnt;
    private Double adjustedSizeProfile;
    private Integer isEligible;
}
