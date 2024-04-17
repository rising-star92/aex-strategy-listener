package com.walmart.aex.strategy.listener.service;


import com.walmart.aex.strategy.listener.dto.*;
import com.walmart.aex.strategy.listener.dto.midas.FinelineRankMetricsDTO;
import com.walmart.aex.strategy.listener.dto.midas.FinelineRankMetricsPayloadDTO;
import com.walmart.aex.strategy.listener.dto.midas.ResponseDTO;
import com.walmart.aex.strategy.listener.dto.midas.ResultDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.*;
import com.walmart.aex.strategy.listener.exception.ClpApListenerException;
import com.walmart.aex.strategy.listener.properties.MidasApiProperties;
import com.walmart.aex.strategy.listener.properties.CredProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
class MidasServiceCallTest {

    @InjectMocks
    private MidasServiceCall midasServiceCall;

    @Mock
    private MidasApiProperties midasProperties;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CredProperties credProperties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(midasProperties.getMidasApiBaseURL()).thenReturn("http://midas-data-api.stg.midas-api.catdev.prod.us.walmart.net/api/workflow/v1/execute");
        Mockito.lenient().when(midasProperties.getAPRankingMetricsQuery()).thenReturn("{\"query\": { \"select\": [ { \"field\": \"*\" } ], \"from\": \"get_apRanking_metrics\", \"params\": { \"l1Id\": \"%s\", \"l2Id\": \"%s\", \"finelineNbr\": \"%s\", \"season\": \"%s\", \"fiscalYear\": \"%s\"}}}");
        Mockito.lenient().when(midasProperties.getOnlineSizeProfilesQuery()).thenReturn("{\"query\":{\"select\":[{\"field\":\"*\"}],\"from\":\"get_ecomm_size_profiles\",\"params\":{\"season\":\"%s\",\"deptNbr\":\"%s\",\"baseItemId\":\"%s\",\"colorFamily\":\"%s\"}}}");
        Mockito.lenient().when(midasProperties.getOnlineSizeProfilesV2Query()).thenReturn("{\"query\":{\"select\":[{\"field\":\"*\"}],\"from\":\"get_ecomm_size_profiles_v2\",\"params\":{\"season\":\"%s\",\"deptNbr\":\"%s\",\"baseItemId\":\"%s\",\"colorFamilies\":\"%s\"}}}");
        Mockito.lenient().when(midasProperties.getStoreSizeProfilesQuery()).thenReturn("{\"query\":{\"select\":[{\"field\":\"*\"}],\"from\":\"get_store_size_profiles\",\"params\":{\"season\":\"%s\",\"deptNbr\":\"%s\",\"finelineNbr\":\"%s\",\"colorFamily\":\"%s\"}}}");
        Mockito.lenient().when(midasProperties.getStoreSizeProfilesV2Query()).thenReturn("{\"query\":{\"select\":[{\"field\":\"*\"}],\"from\":\"get_store_size_profiles_v2\",\"params\":{\"season\":\"%s\",\"deptNbr\":\"%s\",\"finelineNbr\":\"%s\",\"colorFamilies\":\"%s\",\"year\":\"%s\"}}}");
    }

    @Test
    void invokeMidasApiSuccessTest(){
        String planDesc = "S3 - FYE 2023";
        HeaderDTO headerDTO = new HeaderDTO();
        headerDTO.setType("update");
        headerDTO.setSource("AEX-CLP");
        headerDTO.setTimestamp(1559278957L);
        ChangeScopeDTO changeScopeDTO = new ChangeScopeDTO();
        List<String> updatedAtrributes = new ArrayList<>();
        changeScopeDTO.setUpdatedAttributes(updatedAtrributes);
        StrongKeyDTO strongKeyDTO = new StrongKeyDTO();
        strongKeyDTO.setLvl0Nbr(1);
        strongKeyDTO.setLvl1Nbr(23);
        strongKeyDTO.setLvl2Nbr(3670);
        strongKeyDTO.setLvl3Nbr(1234);
        strongKeyDTO.setLvl4Nbr(12345);
        StrongKeyFinelineDTO finelineDTO = new StrongKeyFinelineDTO();
        finelineDTO.setFinelineId(333L);
        List<StrongKeyStyleDTO> styleDTOList = new ArrayList<>();
        StrongKeyStyleDTO styleDTO = new StrongKeyStyleDTO();
        styleDTO.setStyleId("23_34_2_23_001");
        List<String> ccIds = new ArrayList<>();
        ccIds.add("23_34_2_23_001");
        styleDTO.setCcIds(ccIds);
        styleDTOList.add(styleDTO);
        finelineDTO.setStyles(styleDTOList);
        strongKeyDTO.setFineline(finelineDTO);
        changeScopeDTO.setStrongKeys(strongKeyDTO);
        headerDTO.setChangeScope(changeScopeDTO);

        FinelineRankMetricsDTO finelineRankMetricsDTO = new FinelineRankMetricsDTO();
        FinelineRankMetricsPayloadDTO finelineRankMetricsPayloadDTO = new FinelineRankMetricsPayloadDTO();
        ResultDTO resultDTO = new ResultDTO();
        List<ResponseDTO> responseDTOList = new ArrayList<>();
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setAnalyticsClusterId(1);
        responseDTO.setFinelineNbr(333L);
        responseDTO.setForecastedDemandSales((double) 1234);
        responseDTO.setForecastedDemandUnits(12345L);
        responseDTO.setOnHandQty(0L);
        responseDTO.setRank(1);
        responseDTO.setSalesAmtLastYr((double) 1234);
        responseDTO.setSalesToStockRatio((double) 1435);
        responseDTO.setSalesUnitsLastYr(12345L);
        responseDTOList.add(responseDTO);
        resultDTO.setResponse(responseDTOList);
        finelineRankMetricsPayloadDTO.setResult(resultDTO);
        finelineRankMetricsDTO.setPayload(finelineRankMetricsPayloadDTO);

        ResponseEntity entity = new ResponseEntity(finelineRankMetricsDTO, HttpStatus.CREATED);

        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);
        midasServiceCall.invokeMidasApi(headerDTO, planDesc);
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any());
    }

    @Test
    void invokeMidasApiBadRequestTest() {

        String planDesc = "S3 - FYE 2023";

        HeaderDTO headerDTO = new HeaderDTO();
        headerDTO.setType("update");
        headerDTO.setSource("AEX-CLP");
        headerDTO.setTimestamp(1559278957L);
        ChangeScopeDTO changeScopeDTO = new ChangeScopeDTO();
        List<String> updatedAtrributes = new ArrayList<>();
        changeScopeDTO.setUpdatedAttributes(updatedAtrributes);
        StrongKeyDTO strongKeyDTO = new StrongKeyDTO();
        strongKeyDTO.setLvl0Nbr(1);
        strongKeyDTO.setLvl1Nbr(23);
        strongKeyDTO.setLvl2Nbr(3670);
        strongKeyDTO.setLvl3Nbr(1234);
        strongKeyDTO.setLvl4Nbr(12345);
        StrongKeyFinelineDTO strongKeyFinelineDTO = new StrongKeyFinelineDTO();
        strongKeyFinelineDTO.setFinelineId(333L);
        List<StrongKeyStyleDTO> strongKeyStyleDTOS = new ArrayList<>();
        StrongKeyStyleDTO styleDTO = new StrongKeyStyleDTO();
        styleDTO.setStyleId("23_34_2_23_001");
        List<String> ccIds = new ArrayList<>();
        ccIds.add("23_34_2_23_001");
        styleDTO.setCcIds(ccIds);
        strongKeyStyleDTOS.add(styleDTO);
        strongKeyFinelineDTO.setStyles(strongKeyStyleDTOS);
        strongKeyDTO.setFineline(strongKeyFinelineDTO);
        changeScopeDTO.setStrongKeys(strongKeyDTO);
        headerDTO.setChangeScope(changeScopeDTO);

        String response = null;
        ResponseEntity entity = new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);
        midasServiceCall.invokeMidasApi(headerDTO, planDesc);
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any());
    }

    @Test
    void invokeMidasApiToGetProfilesFromOnlineTest(){
        OnlineSizeProfileRequestDTO request = new OnlineSizeProfileRequestDTO();
        request.setBaseItemId("2679268L");
        request.setColorFamily("Black");
        request.setSeason("S3");
        request.setDeptNbr(10L);

        OnlineSizeProfileDataDTO onlineSizeProfileDTO = new OnlineSizeProfileDataDTO();
        OnlineSizeProfilePayloadDTO onlineSizeProfilePayloadDTO = new OnlineSizeProfilePayloadDTO();
        OnlineSizeProfileResultDTO resultDTO = new OnlineSizeProfileResultDTO();
        List<OnlineSizeResponseDTO> responseDTOList = new ArrayList<>();
        OnlineSizeResponseDTO responseDTO = new OnlineSizeResponseDTO();
        responseDTO.setSeason("S3");
        responseDTO.setDept_nbr(23L);
        responseDTO.setBase_itm_id(2679268L);
        responseDTO.setColor_family("Black");
        responseDTO.setCluster_id(0);
        responseDTO.setSize_desc("2XL");
        responseDTO.setSize_profile(0.0);
        responseDTO.setSeason_int(2);
        responseDTO.setPlanning_year(2023L);
        responseDTOList.add(responseDTO);
        resultDTO.setResponse(responseDTOList);
        onlineSizeProfilePayloadDTO.setResult(resultDTO);
        onlineSizeProfileDTO.setPayload(onlineSizeProfilePayloadDTO);

        ResponseEntity entity = new ResponseEntity(onlineSizeProfileDTO, HttpStatus.CREATED);
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);
        midasServiceCall.getOnlineSizeProfiles(request);
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any());
    }

    @Test
    void invokeMidasApiToGetProfilesFromOnlineV2Test(){
        OnlineSizeProfileRequestDTO request = new OnlineSizeProfileRequestDTO();
        request.setBaseItemIds(new HashSet<>(Arrays.asList("2679268L","123L")));
        request.setColorFamily("Black");
        request.setSeason("S3");
        request.setDeptNbr(10L);

        OnlineSizeProfileDataDTO onlineSizeProfileDTO = new OnlineSizeProfileDataDTO();
        OnlineSizeProfilePayloadDTO onlineSizeProfilePayloadDTO = new OnlineSizeProfilePayloadDTO();
        OnlineSizeProfileResultDTO resultDTO = new OnlineSizeProfileResultDTO();
        List<OnlineSizeResponseDTO> responseDTOList = new ArrayList<>();
        OnlineSizeResponseDTO responseDTO = new OnlineSizeResponseDTO();
        responseDTO.setSeason("S3");
        responseDTO.setDept_nbr(23L);
        responseDTO.setBase_itm_id(2679268L);
        responseDTO.setColor_family("Black");
        responseDTO.setCluster_id(0);
        responseDTO.setSize_desc("2XL");
        responseDTO.setSize_profile(0.0);
        responseDTO.setSeason_int(2);
        responseDTO.setPlanning_year(2023L);
        responseDTOList.add(responseDTO);
        resultDTO.setResponse(responseDTOList);
        onlineSizeProfilePayloadDTO.setResult(resultDTO);
        onlineSizeProfileDTO.setPayload(onlineSizeProfilePayloadDTO);

        ResponseEntity entity = new ResponseEntity(onlineSizeProfileDTO, HttpStatus.CREATED);
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);
        midasServiceCall.getOnlineSizeProfilesV2(request);
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any());
    }

    @Test
    void invokeMidasApiToGetProfilesFromStoreTest(){
        StoreSizeProfileRequestDTO request = new StoreSizeProfileRequestDTO();
        request.setFinelineNbr(123L);
        request.setColorFamily("Black");
        request.setSeason("S3");
        request.setDeptNbr(10L);

        StoreSizeProfileDataDTO storeSizeProfileDTO = new StoreSizeProfileDataDTO();
        StoreSizeProfilePayloadDTO onlineSizeProfilePayloadDTO = new StoreSizeProfilePayloadDTO();
        StoreSizeProfileResultDTO resultDTO = new StoreSizeProfileResultDTO();
        List<StoreSizeResponseDTO> responseDTOList = new ArrayList<>();
        StoreSizeResponseDTO responseDTO = new StoreSizeResponseDTO();
        responseDTO.setSeason("S3");
        responseDTO.setDept_nbr(23L);
        responseDTO.setFineline_nbr(123);
        responseDTO.setColor_family("Black");
        responseDTO.setCluster_id(0);
        responseDTO.setSize_desc("2XL");
        responseDTO.setSize_profile(0.0);
        responseDTO.setSeason_int(2);
        responseDTO.setPlanning_year(2023L);
        responseDTO.setDept_catg_nbr(2668L);
        responseDTO.setDept_subcatg_nbr(3557L);
        responseDTOList.add(responseDTO);
        resultDTO.setResponse(responseDTOList);
        onlineSizeProfilePayloadDTO.setResult(resultDTO);
        storeSizeProfileDTO.setPayload(onlineSizeProfilePayloadDTO);

        ResponseEntity entity = new ResponseEntity(storeSizeProfileDTO, HttpStatus.CREATED);
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);
        midasServiceCall.getStoreSizeProfiles(request);
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any());
    }

    @Test
    void invokeMidasApiToGetProfilesFromStoreV2Test(){
        StoreSizeProfileRequestDTO request = new StoreSizeProfileRequestDTO();
        request.setFinelineNbr(123L);
        request.setColorFamilies(Set.of("Black", "Blue","Red"));
        request.setSeason("S3");
        request.setDeptNbr(10L);

        StoreSizeProfileDataDTO storeSizeProfileDTO = new StoreSizeProfileDataDTO();
        StoreSizeProfilePayloadDTO onlineSizeProfilePayloadDTO = new StoreSizeProfilePayloadDTO();
        StoreSizeProfileResultDTO resultDTO = new StoreSizeProfileResultDTO();
        List<StoreSizeResponseDTO> responseDTOList = new ArrayList<>();
        StoreSizeResponseDTO responseDTO = new StoreSizeResponseDTO();
        responseDTO.setSeason("S3");
        responseDTO.setDept_nbr(23L);
        responseDTO.setFineline_nbr(123);
        responseDTO.setColor_family("Black");
        responseDTO.setCluster_id(0);
        responseDTO.setSize_desc("2XL");
        responseDTO.setSize_profile(0.0);
        responseDTO.setSeason_int(2);
        responseDTO.setPlanning_year(2023L);
        responseDTO.setDept_catg_nbr(2668L);
        responseDTO.setDept_subcatg_nbr(3557L);
        responseDTOList.add(responseDTO);
        resultDTO.setResponse(responseDTOList);
        onlineSizeProfilePayloadDTO.setResult(resultDTO);
        storeSizeProfileDTO.setPayload(onlineSizeProfilePayloadDTO);

        ResponseEntity entity = new ResponseEntity(storeSizeProfileDTO, HttpStatus.CREATED);
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);
        List<StoreSizeResponseDTO> storeSizeResponseDTOS = midasServiceCall.getStoreSizeProfilesV2(request);
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any());
        assertNotNull(storeSizeResponseDTOS);
        assertEquals(1, storeSizeResponseDTOS.size());
    }

    @Test
    void exceptionInStoreMidasCall() {
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenThrow(new RuntimeException());
        StoreSizeProfileRequestDTO request = new StoreSizeProfileRequestDTO(null, "Black", "S3", -1L, "2022");
        ClpApListenerException exception = assertThrows(ClpApListenerException.class,
                () -> midasServiceCall.getStoreSizeProfiles(request));
        assertEquals("Exception in Getting Store profile data from Midas for the given Input", exception.getMessage());
    }

    @Test
    void exceptionInOnlineMidasCall() {
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenThrow(new RuntimeException());
        OnlineSizeProfileRequestDTO request = new OnlineSizeProfileRequestDTO(null, "Black", "S3", -1L, "2022");
        ClpApListenerException exception = assertThrows(ClpApListenerException.class,
                () -> midasServiceCall.getOnlineSizeProfiles(request));
        assertEquals("Exception in Getting Online Size profile", exception.getMessage());
    }

    @Test
    void onlineSizeProfileMidasRequestProperlyFormatted() {
        OnlineSizeProfileDataDTO response = new OnlineSizeProfileDataDTO();
        ResponseEntity entity = new ResponseEntity(response, HttpStatus.CREATED);
        OnlineSizeProfileRequestDTO request = new OnlineSizeProfileRequestDTO();
        request.setBaseItemId("469468035");
        request.setSeason("S1");
        request.setDeptNbr(34L);
        request.setColorFamily("Beige");

        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);

        midasServiceCall.getOnlineSizeProfiles(request);
        Mockito.verify(restTemplate).exchange(Mockito.anyString(), Mockito.any(), captor.capture(), Mockito.<Class<String>>any());
        String expected = "{\"query\":{\"select\":[{\"field\":\"*\"}],\"from\":\"get_ecomm_size_profiles\",\"params\":{\"season\":\"'S1'\",\"deptNbr\":\"34\",\"baseItemId\":\"469468035\",\"colorFamily\":\"'Beige'\"}}}";
        String actual = captor.getValue().getBody();
        assertEquals(expected, actual);
    }

    @Test
    void storeSizeProfileMidasRequestProperlyFormatted() {
        StoreSizeProfileDataDTO response = new StoreSizeProfileDataDTO();
        ResponseEntity entity = new ResponseEntity(response, HttpStatus.CREATED);
        StoreSizeProfileRequestDTO request = new StoreSizeProfileRequestDTO();
        request.setFinelineNbr(1233L);
        request.setSeason("S1");
        request.setDeptNbr(34L);
        request.setColorFamily("Beige");

        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenReturn(entity);
        midasServiceCall.getStoreSizeProfiles(request);
        Mockito.verify(restTemplate).exchange(Mockito.anyString(), Mockito.any(), captor.capture(), Mockito.<Class<String>>any());
        String expected = "{\"query\":{\"select\":[{\"field\":\"*\"}],\"from\":\"get_store_size_profiles\",\"params\":{\"season\":\"'S1'\",\"deptNbr\":\"34\",\"finelineNbr\":\"1233\",\"colorFamily\":\"'Beige'\"}}}";
        String actual = captor.getValue().getBody();
        assertEquals(expected, actual);
    }
}
