package com.walmart.aex.strategy.listener.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.walmart.aex.strategy.listener.service.APConsumerService;
import com.walmart.aex.strategy.listener.service.PlanDefinitionService;
import com.walmart.aex.strategy.listener.utils.CommonUtils;
import com.walmart.aex.strategy.listener.utils.StrategyListenerConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@Slf4j
public class ProcessMessageController {
    @Autowired
    private APConsumerService apConsumerService;

    @Autowired
    private PlanDefinitionService planDefinitionService;

    @PostMapping(path = "/retry")
    public ResponseEntity<?> processStartegyMessage(@RequestBody String payload) throws JsonProcessingException {
        try {
            String message = payload.replace("\\", "");
            message = CommonUtils.removeDoubleQuotes(message);
            if(!message.isEmpty()) {
                if (message.contains(StrategyListenerConstants.PLAN_DEFINITION_CREATE) || message.contains(StrategyListenerConstants.PLAN_DEFINITION_MODIFY)) {
                    planDefinitionService.processMessage(message);
                } else {
                    apConsumerService.processMessage(message);
                }
            }
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        catch (JsonProcessingException jsonProcessingException){
            log.error("Exception occurred when processing strategy message: {}", jsonProcessingException.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        catch (Exception ex){
            log.error("Exception occurred when processing strategy message: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
