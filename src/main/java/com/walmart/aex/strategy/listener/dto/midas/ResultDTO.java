package com.walmart.aex.strategy.listener.dto.midas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ResultDTO {
    private List<ResponseDTO> response;
}
