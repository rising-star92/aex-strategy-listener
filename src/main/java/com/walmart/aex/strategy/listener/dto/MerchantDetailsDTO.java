package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MerchantDetailsDTO {

    private String merchantAlias;
    private String firstName;
    private String lastName;
    private String merchantEmail;
    private String domain;
}
