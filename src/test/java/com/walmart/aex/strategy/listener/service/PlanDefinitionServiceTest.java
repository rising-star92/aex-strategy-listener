package com.walmart.aex.strategy.listener.service;


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
class PlanDefinitionServiceTest {

    @InjectMocks
    PlanDefinitionService planDefinitionService;

    @Mock
    StrategyServiceProperties strategyServiceProperties;

    @Mock
    RestTemplate restTemplate;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        Field field2 = ReflectionUtils.findField(PlanDefinitionService.class, "strategyServiceProperties");
        field2.setAccessible(true);
        field2.set(planDefinitionService, strategyServiceProperties);

        Mockito.when(strategyServiceProperties.getStrategyApiBaseURL()).thenReturn("testBaseUrl");
    }

    @Test
    void testPostStrategyCreatePayloadSuccess() throws IOException {
        ResponseEntity entity = new ResponseEntity(HttpStatus.CREATED);
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);

        planDefinitionService.postEventPlanDefinitionService("PLAN_DEFINITION_CREATE");
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(), Mockito.any(),Mockito.any(HttpEntity.class), (Class<Object>) Mockito.any());
    }

    @Test
    void testPostStrategyCreatePayloadFailure() {
        Assertions.assertThrows(ClpApListenerException.class, () -> planDefinitionService.postEventPlanDefinitionService(""));
    }

    @Test
    void testPostStrategyUpdatePayloadSuccess() throws IOException {
        ResponseEntity entity = new ResponseEntity(HttpStatus.OK);
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);

        planDefinitionService.postEventPlanDefinitionService("PLAN_DEFINITION_MODIFY");
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(), Mockito.any(),Mockito.any(HttpEntity.class), (Class<Object>) Mockito.any());
    }
}
