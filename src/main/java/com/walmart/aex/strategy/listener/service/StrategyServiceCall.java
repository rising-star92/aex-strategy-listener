package com.walmart.aex.strategy.listener.service;

import com.walmart.aex.strategy.listener.dto.strategy.request.StrategyDeletePayloadDTO;
import com.walmart.aex.strategy.listener.dto.strategy.request.StrategyPayloadDTO;
import com.walmart.aex.strategy.listener.exception.ClpApListenerException;
import com.walmart.aex.strategy.listener.properties.StrategyServiceProperties;
import io.strati.ccm.utils.client.annotation.ManagedConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class StrategyServiceCall {
    @Autowired
    RestTemplate restTemplate;

    @ManagedConfiguration
    public StrategyServiceProperties strategyServiceProperties;

    @Retryable(exclude = HttpClientErrorException.BadRequest.class, backoff = @Backoff(delay = 10000))
    public String postEventStrategyService(StrategyPayloadDTO strategyPayloadDTO, HttpMethod httpMethod) {
        String responseMsg = null;
        try {
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<StrategyPayloadDTO> entity = new HttpEntity<>(strategyPayloadDTO,headers);
            log.info("Calling AEX-STRATEGY-SERVICE API method : {} ", httpMethod.name());
            ResponseEntity<String> responseEntity = restTemplate.exchange(strategyServiceProperties.getStrategyApiBaseURL().concat("/strategyService"),
                    httpMethod, entity, String.class);
            responseMsg = responseEntity.getBody();
            HttpStatus statusCode = responseEntity.getStatusCode();
            if (HttpStatus.CREATED == statusCode || HttpStatus.OK == statusCode) {
                log.info("Create Event Posted Successfully: {}", responseEntity.getBody());
            } else {
                log.info("Error posting Create Event: {}", responseEntity.getBody());
                throw new ClpApListenerException("Error posting event to strategy service: "+ strategyPayloadDTO);
            }
        } catch (Exception e) {
            log.error("Exception while posting Create Event::{} Exception: ", strategyPayloadDTO, e);
            throw new ClpApListenerException("Error posting event to strategy service: "+ strategyPayloadDTO);
        }
        return responseMsg;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "*/*");
        headers.set("WM_CONSUMER.ID", strategyServiceProperties.getStrategyConsumerId());
        headers.set("WM_SVC.NAME", strategyServiceProperties.getStrategyAppKey());
        headers.set("WM_SVC.ENV", strategyServiceProperties.getStrategyEnv());
        return headers;
    }

    @Retryable(exclude = HttpClientErrorException.BadRequest.class, backoff = @Backoff(delay = 10000))
    public String deleteEventStrategyService(StrategyDeletePayloadDTO strategyDeletePayloadDTO, HttpMethod delete) {
        String responseMsg = null;
        try {
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<StrategyDeletePayloadDTO> entity = new HttpEntity<>(strategyDeletePayloadDTO,headers);
            log.info("Calling AEX-STRATEGY-SERVICE API method : {} ", delete.name());
            ResponseEntity<String> responseEntity = restTemplate.exchange(strategyServiceProperties.getStrategyApiBaseURL().concat("/strategyService"),
                    delete, entity, String.class);
            responseMsg = responseEntity.getBody();
            HttpStatus statusCode = responseEntity.getStatusCode();
            if (HttpStatus.CREATED == statusCode || HttpStatus.OK == statusCode) {
                log.info("Delete Event Posted Successfully: {}", responseEntity.getBody());
            } else {
                log.info("Error posting Delete Event: {}", responseEntity.getBody());
                throw new ClpApListenerException("Error posting delete event to strategy service: "+ strategyDeletePayloadDTO);
            }
        } catch (Exception e) {
            log.error("Exception while posting Delete Event::{} Exception: ", strategyDeletePayloadDTO, e);
            throw new ClpApListenerException("Error posting delete event to strategy service: "+ strategyDeletePayloadDTO);
        }
        return responseMsg;
    }

    @Recover
    public String recover(Exception e, StrategyPayloadDTO strategyPayloadDTO, HttpMethod httpMethod) {
        throw new ClpApListenerException("TimeOut posting event to strategy service: " + strategyPayloadDTO, e);
    }
}
