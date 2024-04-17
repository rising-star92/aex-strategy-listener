package com.walmart.aex.strategy.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SizeDetailRequestDTO {
    private String attributeType;
    private Integer categoryId;
    private Long departmentId;
    private String season;
}
