package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FinancialAttributesDTO {
    private String transactableEndWk;
    private String transactableStartWk;
    private Long transactableStartWkNbr;
    private Long transactableEndWkNbr;
}
