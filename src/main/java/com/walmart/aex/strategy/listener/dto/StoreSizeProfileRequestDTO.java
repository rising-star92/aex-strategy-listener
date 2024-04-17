package com.walmart.aex.strategy.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreSizeProfileRequestDTO {
    private Long finelineNbr;
    private String colorFamily;
    private String season;
    private Long deptNbr;
    private Long catgNbr;
    private Long subCatgNbr;
    private String year;
    private Set<String> colorFamilies;

    public StoreSizeProfileRequestDTO(Long finelineNbr, String colorFamily, String season, Long deptNbr, String year) {
        this.finelineNbr = finelineNbr;
        this.colorFamily = colorFamily;
        this.season = season;
        this.deptNbr = deptNbr;
        this.year = year;
    }

    public StoreSizeProfileRequestDTO(Long finelineNbr, String season, Long deptNbr, Long catgNbr, Long subCatgNbr, String year, Set<String> colorFamilies) {
        this.finelineNbr = finelineNbr;
        this.season = season;
        this.deptNbr = deptNbr;
        this.catgNbr = catgNbr;
        this.subCatgNbr = subCatgNbr;
        this.year = year;
        this.colorFamilies = colorFamilies;
    }
}
