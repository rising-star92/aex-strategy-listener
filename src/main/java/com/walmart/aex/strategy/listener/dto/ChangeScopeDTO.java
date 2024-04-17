package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeScopeDTO {

    private List<String> updatedAttributes;
    private StrongKeyDTO strongKeys;
}
