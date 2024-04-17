package com.walmart.aex.strategy.listener.dto.midas.response;

import lombok.Data;

@Data
public class SizeProfileResponse {
   private String season;
   private Long dept_nbr;
   private String color_family;
   private Integer cluster_id;
   private Double size_profile;
   private String size_desc;
}
