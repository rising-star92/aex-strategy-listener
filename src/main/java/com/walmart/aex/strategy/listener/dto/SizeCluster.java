package com.walmart.aex.strategy.listener.dto;

import com.walmart.aex.strategy.listener.dto.strategy.request.ClusterType;
import com.walmart.aex.strategy.listener.dto.strategy.request.SizeProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SizeCluster {
   private ClusterType type;
   private List<SizeProfileDTO> sizeProfiles;
}
