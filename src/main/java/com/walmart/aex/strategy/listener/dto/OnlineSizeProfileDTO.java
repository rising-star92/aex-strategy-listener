package com.walmart.aex.strategy.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineSizeProfileDTO {

	private Long deptNbr;
	private String baseItemId;
	private Integer rpt_lvl_1_id;
	private Integer rpt_lvl_2_id;
	private Integer rpt_lvl_3_id;
	private Integer rpt_lvl_4_id;
	private String colorFamily;
	private Integer clusterId;
	private String sizeDesc;
	private String season;
	private Double sizeProfilePrcnt;
}
