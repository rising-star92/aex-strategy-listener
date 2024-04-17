package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrongKeyStyleDTO {
    private String styleId;
    private String channel;
    private List<CustomerChoiceDTO> customerChoices;
    private List<String> ccIds;
}
