package com.walmart.aex.strategy.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SizeDetailResponseDTO {
    private Integer id;
    private String value;
    private Map<String,String> attributeMap;
}
