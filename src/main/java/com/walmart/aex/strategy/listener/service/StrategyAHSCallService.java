package com.walmart.aex.strategy.listener.service;

import com.walmart.aex.strategy.listener.dto.SizeDetailRequestDTO;
import com.walmart.aex.strategy.listener.dto.SizeDetailResponseDTO;
import com.walmart.aex.strategy.listener.exception.ClpApListenerException;
import com.walmart.aex.strategy.listener.properties.AHSApiProperties;
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

@Slf4j
@Service
public class StrategyAHSCallService {

    @ManagedConfiguration
    private AHSApiProperties ahsApiProperties;

    @Autowired
    public RestTemplate restTemplate ;

    /***
     * Below function will make POST call to AHS hierarchy API and get size details  ahsApiProperties.getAHSHeaderSizeAttribute()  ahsApiProperties.getAHSSizeURL()
     * @param categoryId
     * @param departmentId
     * @return SizeDetailResponseDTO
     */
    @Retryable(exclude = HttpClientErrorException.BadRequest.class, backoff = @Backoff(delay = 10000))
    public SizeDetailResponseDTO[] getSizeDetailsFromAHS(Integer categoryId, Long departmentId) {
        SizeDetailResponseDTO[] responseMsg = null;
        try {
            SizeDetailRequestDTO request = new SizeDetailRequestDTO(ahsApiProperties.getAHSHeaderSizeAttribute(), categoryId, departmentId, "");
            String url = ahsApiProperties.getAHSSizeURL();
            log.info("Invoking AHS API for Size details with URL : {} and query : {}", url, request);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.set("WM_CONSUMER.ID", ahsApiProperties.getAHSHeaderCustomerID());
            headers.set("WM_SVC.NAME", ahsApiProperties.getAHSHeaderServiceName());
            headers.set("WM_SVC.ENV", ahsApiProperties.getAHSHeaderEnvironment());
            HttpEntity<?> entity = new HttpEntity<>(request, headers);
            log.info("Calling AEX-STRATEGY-SERVICE API method for size profile: {} ", HttpMethod.POST.name());
            ResponseEntity<SizeDetailResponseDTO[]> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, SizeDetailResponseDTO[].class);
            responseMsg = responseEntity.getBody();

            HttpStatus statusCode = responseEntity.getStatusCode();
            if (HttpStatus.CREATED == statusCode || HttpStatus.OK == statusCode) {
                log.info("Create Event Posted Successfully to AHS service for size profile: {}", responseEntity.getBody());
            } else {
                log.info("Error posting Create Event to AHS service for size profile: {}", responseEntity.getBody());
                throw new ClpApListenerException("Error posting event to AHS size service: " + request);
            }
        } catch (Exception e) {
            log.error("Exception while posting Create Event: {} Exception: ", e);
            throw new ClpApListenerException("Error posting event to strategy service: ");
        }
        return responseMsg;
    }

    @Recover
    public String recover(Exception e, SizeDetailRequestDTO sizeDetailRequestDTO, HttpMethod httpMethod) {
        throw new ClpApListenerException("TimeOut posting event to strategy service: " + sizeDetailRequestDTO, e);
    }
}
