package com.walmart.aex.strategy.listener.dto.strategy.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterType {
   private Integer analyticsClusterId;
   private String analyticsClusterDesc;
}
