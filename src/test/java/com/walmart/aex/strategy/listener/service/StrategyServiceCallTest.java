package com.walmart.aex.strategy.listener.service;

import com.walmart.aex.strategy.listener.dto.strategy.request.StrategyPayloadDTO;
import com.walmart.aex.strategy.listener.exception.ClpApListenerException;
import com.walmart.aex.strategy.listener.properties.StrategyServiceProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Field;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
class StrategyServiceCallTest {

    @InjectMocks
    StrategyServiceCall strategyServiceCall;

    @Mock
    StrategyServiceProperties strategyServiceProperties;

    @Mock
    RestTemplate restTemplate;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        Field field2 = ReflectionUtils.findField(StrategyServiceCall.class, "strategyServiceProperties");
        field2.setAccessible(true);
        field2.set(strategyServiceCall, strategyServiceProperties);

        Mockito.when(strategyServiceProperties.getStrategyApiBaseURL()).thenReturn("testBaseUrl");
    }

    @Test
    void testPostStrategyCreatePayloadSuccess() throws IOException {
        StrategyPayloadDTO strategyPayloadDTO = new StrategyPayloadDTO();
        ResponseEntity entity = new ResponseEntity(HttpStatus.CREATED);
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);

        strategyServiceCall.postEventStrategyService(strategyPayloadDTO, HttpMethod.POST);
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(), Mockito.any(),Mockito.any(HttpEntity.class), (Class<Object>) Mockito.any());
    }

    @Test
    void testPostStrategyCreatePayloadFailure() throws IOException {
        StrategyPayloadDTO strategyPayloadDTO = new StrategyPayloadDTO();
        ResponseEntity entity = new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);

        Assertions.assertThrows(ClpApListenerException.class, () -> strategyServiceCall.postEventStrategyService(strategyPayloadDTO, HttpMethod.POST));
    }
}
