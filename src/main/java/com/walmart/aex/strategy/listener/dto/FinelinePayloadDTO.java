package com.walmart.aex.strategy.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FinelinePayloadDTO {
    private Long finelineId;
    private String finelineName;
    private String altFinelineName;
    private String channel;
    private MetricsDTO metrics;
    private LikeAssociationDTO likeAssociation;
    private List<StyleDTO> styles;
}