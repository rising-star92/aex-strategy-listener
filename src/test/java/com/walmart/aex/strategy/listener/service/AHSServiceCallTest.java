package com.walmart.aex.strategy.listener.service;

import com.walmart.aex.strategy.listener.dto.SizeDetailResponseDTO;
import com.walmart.aex.strategy.listener.exception.ClpApListenerException;
import com.walmart.aex.strategy.listener.properties.AHSApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.web.client.RestTemplate;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
class AHSServiceCallTest {
    @InjectMocks
    StrategyAHSCallService ahsServiceCall;

    @Mock
    AHSApiProperties ahsProperties;

    @Mock
    RestTemplate restTemplate;

    @Test
    void invokeSizeApiSuccessTest(){
        SizeDetailResponseDTO sizeDetailResponseDTO = new SizeDetailResponseDTO();
        SizeDetailResponseDTO[] response = new SizeDetailResponseDTO[1] ;
        sizeDetailResponseDTO.setId(229);
        sizeDetailResponseDTO.setValue("S/M");
        sizeDetailResponseDTO.setAttributeMap(null);
        response[0] = sizeDetailResponseDTO;
        ResponseEntity entity = new ResponseEntity(response, HttpStatus.CREATED);
        Long x = Long.valueOf(12228);
        Mockito.when(ahsProperties.getAHSHeaderEnvironment()).thenReturn("dev");
        Mockito.when(ahsProperties.getAHSSizeURL()).thenReturn("url");
        Mockito.when(ahsProperties.getAHSHeaderCustomerID()).thenReturn("0af23de5-9574-456a-a974-19e5e732bb0c");
        Mockito.when(ahsProperties.getAHSHeaderServiceName()).thenReturn("ATTRIBUTE-HELPER-SERVICE");
        Mockito.when(ahsProperties.getAHSHeaderSizeAttribute()).thenReturn("size");
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);
        ahsServiceCall.getSizeDetailsFromAHS(1,x );
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any());
    }

    @Test
    void invokeSizeApiMissingHeaderTest(){
        SizeDetailResponseDTO sizeDetailResponseDTO = new SizeDetailResponseDTO();
        SizeDetailResponseDTO[] response = new SizeDetailResponseDTO[1] ;
        sizeDetailResponseDTO.setId(229);
        sizeDetailResponseDTO.setValue("S/M");
        sizeDetailResponseDTO.setAttributeMap(null);
        response[0] = sizeDetailResponseDTO;
        ResponseEntity entity = new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        Long x = Long.valueOf(12228);
        Mockito.when(ahsProperties.getAHSSizeURL()).thenReturn("url");
        Mockito.when(ahsProperties.getAHSHeaderCustomerID()).thenReturn("0af23de5");
        Mockito.when(ahsProperties.getAHSHeaderServiceName()).thenReturn("SERVICE");
        Mockito.when(ahsProperties.getAHSHeaderSizeAttribute()).thenReturn("size");
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);
        Assertions.assertThrows(ClpApListenerException.class, () -> ahsServiceCall.getSizeDetailsFromAHS(1,x ));
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any());
    }


}
