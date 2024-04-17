package com.walmart.aex.strategy.listener.service;

import com.walmart.aex.strategy.listener.exception.ClpApListenerException;
import com.walmart.aex.strategy.listener.properties.FeatureConfigProperties;
import com.walmart.aex.strategy.listener.properties.StrategyServiceProperties;
import io.strati.ccm.utils.client.annotation.ManagedConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class PlanDefinitionService {

    RestTemplate restTemplate;

    @ManagedConfiguration
    public StrategyServiceProperties strategyServiceProperties;

    @ManagedConfiguration
    private FeatureConfigProperties featureConfigProperties;

    public PlanDefinitionService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }
    @Retryable(exclude = HttpClientErrorException.BadRequest.class, backoff = @Backoff(delay = 10000))
    public String postEventPlanDefinitionService(String message) {
        String responseMsg = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");
            headers.set("WM_CONSUMER.ID",strategyServiceProperties.getStrategyConsumerId());
            headers.set("WM_SVC.NAME",strategyServiceProperties.getStrategyAppKey());
            headers.set("WM_SVC.ENV",strategyServiceProperties.getStrategyEnv());
            HttpEntity<String> entity = new HttpEntity<>(message,headers);
            log.info("Calling AEX-STRATEGY-SERVICE API PLanDefinition for Insert Event: {}", message);
            String restApiUri = null;

            if(message.contains("PLAN_DEFINITION_CREATE"))
                restApiUri = "/planDefinition";
            else if(message.contains("PLAN_DEFINITION_MODIFY"))
                restApiUri = "/updatePlanStrategy";

            ResponseEntity<String> responseEntity = restTemplate.exchange(strategyServiceProperties.getStrategyApiBaseURL().concat(restApiUri),
                    HttpMethod.POST, entity, String.class);
            responseMsg = responseEntity.getBody();
            HttpStatus statusCode = responseEntity.getStatusCode();
            if (HttpStatus.CREATED == statusCode || HttpStatus.OK == statusCode) {
                log.info("Create Event Posted Successfully: {}", responseEntity.getBody());
            } else {
                log.info("Error posting Create Event: {}", responseEntity.getBody());
                throw new ClpApListenerException("Error posting event to strategy service: "+ message);
            }
        } catch (Exception e) {
            log.error("Exception while posting Create Event::{} Exception: ", message, e);
            throw new ClpApListenerException("Error posting event to strategy service: "+ message);
        }
        return responseMsg;
    }

    public void processMessage(String message) {

        if(message.contains("PLAN_DEFINITION_CREATE")) {
            postEventPlanDefinitionService(message);
        }
        else if(message.contains("PLAN_DEFINITION_MODIFY") && featureConfigProperties.isEnableDeletePlanCategorySubCategory()){
            postEventPlanDefinitionService(message);
        }

    }

}
