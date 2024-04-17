package com.walmart.aex.strategy.listener.dto.midas.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OnlineSizeProfileResultDTO {
    private List<OnlineSizeResponseDTO> response;
}
