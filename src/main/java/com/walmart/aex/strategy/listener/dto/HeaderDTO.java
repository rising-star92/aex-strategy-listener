package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HeaderDTO {

    private String type;
    private String source;
    private Long timestamp;
    private ChangeScopeDTO changeScope;
}
