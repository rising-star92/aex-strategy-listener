package com.walmart.aex.strategy.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class OnlineSizeProfileRequestDTO {

	private String baseItemId;
	private String colorFamily;
	private String season;
	private Long deptNbr;
	private String year;
	private Set<String> colorFamilies;

	private Set<String> baseItemIds;

	public OnlineSizeProfileRequestDTO(String baseItemId, String colorFamily, String season, Long deptNbr, String year) {
		this.baseItemId = baseItemId;
		this.colorFamily = colorFamily;
		this.season = season;
		this.deptNbr = deptNbr;
		this.year = year;
	}

	public OnlineSizeProfileRequestDTO(String baseItemId, String season, Long deptNbr, String year, Set<String> colorFamilies) {
		this.baseItemId = baseItemId;
		this.season = season;
		this.deptNbr = deptNbr;
		this.year = year;
		this.colorFamilies = colorFamilies;
	}

	public OnlineSizeProfileRequestDTO(Set<String> baseItemIds, String season, Long deptNbr, String year, Set<String> colorFamilies) {
		this.baseItemIds = baseItemIds;
		this.season = season;
		this.deptNbr = deptNbr;
		this.year = year;
		this.colorFamilies = colorFamilies;
	}
}
