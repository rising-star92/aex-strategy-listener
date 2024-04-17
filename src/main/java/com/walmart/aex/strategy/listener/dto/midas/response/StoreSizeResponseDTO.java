package com.walmart.aex.strategy.listener.dto.midas.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreSizeResponseDTO extends SizeProfileResponse {
    private Long dept_catg_nbr;
    private Long dept_subcatg_nbr;
    private Integer fineline_nbr;
    private Integer season_int;
    private Long planning_year;
}
