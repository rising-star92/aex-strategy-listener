package com.walmart.aex.strategy.listener.dto.midas.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineSizeResponseDTO extends SizeProfileResponse {
    //The variable names are same as the query result
    private Long base_itm_id;
    private Integer season_int;
    private Long planning_year;
}
