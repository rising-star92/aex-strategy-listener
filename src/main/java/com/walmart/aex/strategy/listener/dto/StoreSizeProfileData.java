package com.walmart.aex.strategy.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreSizeProfileData {
    private String season;
    private Long deptNbr;
    private Long deptCatgNbr;
    private Long deptSubCatgNbr;
    private Integer finelineNbr;
    private String colorFamily;
    private Integer clusterId;
    private String sizeDesc;
    private Double sizeProfile;
}
