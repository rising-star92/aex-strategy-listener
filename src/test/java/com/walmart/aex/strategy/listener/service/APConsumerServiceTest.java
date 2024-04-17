package com.walmart.aex.strategy.listener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.aex.strategy.listener.dto.*;
import com.walmart.aex.strategy.listener.dto.midas.FinelineRankMetricsDTO;
import com.walmart.aex.strategy.listener.dto.midas.FinelineRankMetricsPayloadDTO;
import com.walmart.aex.strategy.listener.dto.midas.ResponseDTO;
import com.walmart.aex.strategy.listener.dto.midas.ResultDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.OnlineSizeProfileDataDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.OnlineSizeProfilePayloadDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.OnlineSizeProfileResultDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.OnlineSizeResponseDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.StoreSizeProfileDataDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.StoreSizeProfilePayloadDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.StoreSizeProfileResultDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.StoreSizeResponseDTO;
import com.walmart.aex.strategy.listener.dto.strategy.request.ClusterType;
import com.walmart.aex.strategy.listener.dto.strategy.request.SizeProfileDTO;
import com.walmart.aex.strategy.listener.dto.strategy.request.StrategyCustomerChoiceDTO;
import com.walmart.aex.strategy.listener.dto.strategy.request.StrategyDTO;
import com.walmart.aex.strategy.listener.dto.strategy.request.StrategyFinelinesDTO;
import com.walmart.aex.strategy.listener.dto.strategy.request.StrategyLvl3DTO;
import com.walmart.aex.strategy.listener.dto.strategy.request.StrategyPayloadDTO;
import com.walmart.aex.strategy.listener.dto.strategy.request.StrategyStyleDTO;
import com.walmart.aex.strategy.listener.properties.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
class APConsumerServiceTest {

    @InjectMocks
    private APConsumerService apConsumerService;

    @Mock
    private KafkaProperties kafkaProperties;

    @Mock
    private MidasServiceCall midasServiceCall;

    @Mock
    private StrategyServiceCall strategyServiceCall;

    @Mock
    private StrategyServiceProperties strategyServiceProperties;

    @Mock
    private StrategyAHSCallService strategyAHSCallService;

    @Mock
    private AHSApiProperties ahsApiProperties;

    @Mock
    private SizePackProperties sizePackProperties;

    @Mock
    private FeatureConfigProperties featureConfigProperties;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        Field field2 = ReflectionUtils.findField(APConsumerService.class, "objectMapper");
        field2.setAccessible(true);
        field2.set(apConsumerService, new ObjectMapper());

        Field field = ReflectionUtils.findField(APConsumerService.class, "midasServiceCall");
        field.setAccessible(true);
        field.set(apConsumerService, midasServiceCall);

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

        Field field3 = ReflectionUtils.findField(APConsumerService.class, "strategyServiceCall");
        field3.setAccessible(true);
        field3.set(apConsumerService, strategyServiceCall);

        Field field6 = ReflectionUtils.findField(APConsumerService.class, "ahsApiProperties");
        field6.setAccessible(true);
        field6.set(apConsumerService, ahsApiProperties);

        Mockito.lenient().when(ahsApiProperties.getSizePackReleaseFlag()).thenReturn("false");

        //Setting kafkaProperties field to apConsumerService to avoid NPE.
        Field field7 = ReflectionUtils.findField(APConsumerService.class, "kafkaProperties");
        field7.setAccessible(true);
        field7.set(apConsumerService, kafkaProperties);


        Mockito.lenient().when(midasServiceCall.invokeMidasApi(any(HeaderDTO.class), any(String.class))).thenReturn(finelineRankMetricsDTO);
        Mockito.lenient().when(strategyServiceCall.postEventStrategyService(any(),Mockito.any(HttpMethod.class))).thenReturn("success");

        Field field4 = ReflectionUtils.findField(APConsumerService.class, "strategyServiceProperties");
        field4.setAccessible(true);
        field4.set(apConsumerService, strategyServiceProperties);

        List<String> fixtureList = new ArrayList<>();
        fixtureList.add("prefferedFixtyreType");

        Mockito.lenient().when(strategyServiceProperties.getAcceptedFixtureStrategyUpdates()).thenReturn(fixtureList);

        Field field8 = ReflectionUtils.findField(APConsumerService.class, "sizePackProperties");
        field8.setAccessible(true);
        field8.set(apConsumerService, sizePackProperties);

        List<String> acceptedUpdatesSPList = new ArrayList<>();
        acceptedUpdatesSPList.add("channel");

        Mockito.lenient().when(sizePackProperties.isDefaultPlanDesc()).thenReturn(false);
        Mockito.lenient().when(sizePackProperties.getAcceptedSizePackUpdates()).thenReturn(acceptedUpdatesSPList);

        SizeDetailResponseDTO[] result =  new SizeDetailResponseDTO[1];
        SizeDetailResponseDTO sizeData = new SizeDetailResponseDTO();
        sizeData.setId(1);
        sizeData.setValue("abc");
        result[0] = sizeData;
        Field field5 = ReflectionUtils.findField(APConsumerService.class, "strategyAHSCallService");
        field5.setAccessible(true);
        field5.set(apConsumerService, strategyAHSCallService);
        Mockito.lenient().when(strategyAHSCallService.getSizeDetailsFromAHS(12228,34L)).thenReturn(result);

        Field featureField = ReflectionUtils.findField(APConsumerService.class, "featureConfigProperties");
        featureField.setAccessible(true);
        featureField.set(apConsumerService, featureConfigProperties);
        Mockito.lenient().when(featureConfigProperties.isLikeFinelineFeatureFlag()).thenReturn(true);
    }

    @Test
    void processUpdateMessageTest() throws IOException {
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
        FinelineDTO finelineDTO = new FinelineDTO();
        finelineDTO.setFinelineId(333);
        List<StyleDTO> styleDTOList = new ArrayList<>();
        StyleDTO styleDTO = new StyleDTO();
        styleDTO.setStyleNbr("23_34_2_23_001");
        List<String> ccIds = new ArrayList<>();
        ccIds.add("23_34_2_23_001");
        styleDTO.setCcIds(ccIds);
        styleDTOList.add(styleDTO);
        finelineDTO.setStyles(styleDTOList);

        StrongKeyFinelineDTO strongKeyFinelineDTO = new StrongKeyFinelineDTO();
        strongKeyFinelineDTO.setFinelineId(333L);
        strongKeyDTO.setFineline(strongKeyFinelineDTO);

        changeScopeDTO.setStrongKeys(strongKeyDTO);
        headerDTO.setChangeScope(changeScopeDTO);

        apConsumerService.processMessage(readTextFileAsString("clp-ap-listener-contract"));
        Mockito.verify(midasServiceCall, Mockito.times(0)).invokeMidasApi(headerDTO, "S3 - FYE 2023");
    }

    @Test
    void processCreateMessageTest() throws IOException {
        HeaderDTO headerDTO = new HeaderDTO();
        headerDTO.setType("CREATE");
        headerDTO.setSource("AEX-CLP");
        headerDTO.setTimestamp(1652808999736L);
        ChangeScopeDTO changeScopeDTO = new ChangeScopeDTO();
        List<String> updatedAtrributes = new ArrayList<>();
        changeScopeDTO.setUpdatedAttributes(updatedAtrributes);

        StrongKeyDTO strongKeyDTO = new StrongKeyDTO();
        strongKeyDTO.setPlanId(266L);
        strongKeyDTO.setLvl0Nbr(50000);
        strongKeyDTO.setLvl0GenDesc1("Apparel");
        strongKeyDTO.setLvl1Nbr(34);
        strongKeyDTO.setLvl1GenDesc1("D34 - Womens Apparel");
        strongKeyDTO.setLvl2Nbr(6419);
        strongKeyDTO.setLvl2GenDesc1("Plus Womens");
        strongKeyDTO.setLvl3Nbr(12234);
        strongKeyDTO.setLvl4Nbr(31517);
        FinelineDTO finelineDTO = new FinelineDTO();
        finelineDTO.setFinelineId(3019);
        List<StyleDTO> styleDTOList = new ArrayList<>();
        finelineDTO.setStyles(styleDTOList);
        StrongKeyFinelineDTO strongKeyFinelineDTO = new StrongKeyFinelineDTO();
        List<StrongKeyStyleDTO> strongKeyStyleDTOS = new ArrayList<>();
        strongKeyFinelineDTO.setFinelineId(3019L);
        strongKeyFinelineDTO.setStyles(strongKeyStyleDTOS);
        strongKeyDTO.setFineline(strongKeyFinelineDTO);
        changeScopeDTO.setStrongKeys(strongKeyDTO);
        headerDTO.setChangeScope(changeScopeDTO);

        apConsumerService.processMessage(readTextFileAsString("clpMessageCreate"));
        Mockito.verify(midasServiceCall, Mockito.times(1)).invokeMidasApi(any(HeaderDTO.class), any(String.class));
    }

    @Test
    void processInitialLoadMessageTest() throws IOException {
        apConsumerService.processMessage(readTextFileAsString("clpMessageInitialLoad"));
        Mockito.verify(midasServiceCall, Mockito.times(0)).invokeMidasApi(any(HeaderDTO.class), any(String.class));
    }
    @Test
    void processCreateOnlineMessageTest() throws IOException {
        Mockito.when(ahsApiProperties.getSizePackReleaseFlag()).thenReturn("true");
        String planDesc = "S3 - FYE 2023";
        HeaderDTO headerDTO = new HeaderDTO();
        headerDTO.setType("CREATE");
        headerDTO.setSource("AEX-CLP");
        headerDTO.setTimestamp(1652808999736L);
        ChangeScopeDTO changeScopeDTO = new ChangeScopeDTO();
        List<String> updatedAtrributes = new ArrayList<>();
        changeScopeDTO.setUpdatedAttributes(updatedAtrributes);

        StrongKeyDTO strongKeyDTO = new StrongKeyDTO();
        strongKeyDTO.setLvl0Nbr(50000);
        strongKeyDTO.setLvl0GenDesc1("Apparel");
        strongKeyDTO.setLvl1Nbr(34);
        strongKeyDTO.setLvl1GenDesc1("D34 - Womens Apparel");
        strongKeyDTO.setLvl2Nbr(6419);
        strongKeyDTO.setLvl2GenDesc1("Plus Womens");
        strongKeyDTO.setLvl3Nbr(12234);
        strongKeyDTO.setLvl4Nbr(31517);
        FinelineDTO finelineDTO = new FinelineDTO();
        finelineDTO.setFinelineId(3019);
        List<StyleDTO> styleDTOList = new ArrayList<>();
        finelineDTO.setStyles(styleDTOList);
        StrongKeyFinelineDTO strongKeyFinelineDTO = new StrongKeyFinelineDTO();
        strongKeyFinelineDTO.setFinelineId(333L);
        strongKeyDTO.setFineline(strongKeyFinelineDTO);
        changeScopeDTO.setStrongKeys(strongKeyDTO);
        headerDTO.setChangeScope(changeScopeDTO);

        apConsumerService.processMessage(readTextFileAsString("clpMessageCreateOnline"));
        Mockito.verify(midasServiceCall, Mockito.times(0)).invokeMidasApi(headerDTO, planDesc);
    }

    @Test
    void processUpdateChannelTest() throws IOException {
        HeaderDTO headerDTO = new HeaderDTO();
        headerDTO.setType("UPDATE");
        headerDTO.setSource("AEX-CLP");
        headerDTO.setTimestamp(1652808999736L);
        ChangeScopeDTO changeScopeDTO = new ChangeScopeDTO();
        List<String> updatedAtrributes = new ArrayList<>();
        updatedAtrributes.add("channel");
        changeScopeDTO.setUpdatedAttributes(updatedAtrributes);

        StrongKeyDTO strongKeyDTO = new StrongKeyDTO();
        strongKeyDTO.setPlanId(266L);
        strongKeyDTO.setLvl0Nbr(50000);
        strongKeyDTO.setLvl0GenDesc1("Apparel");
        strongKeyDTO.setLvl1Nbr(34);
        strongKeyDTO.setLvl1GenDesc1("D34 - Womens Apparel");
        strongKeyDTO.setLvl2Nbr(6419);
        strongKeyDTO.setLvl2GenDesc1("Plus Womens");
        strongKeyDTO.setLvl3Nbr(12234);
        strongKeyDTO.setLvl4Nbr(31517);
        FinelineDTO finelineDTO = new FinelineDTO();
        finelineDTO.setFinelineId(3019);
        List<StyleDTO> styleDTOList = new ArrayList<>();
        StyleDTO styleDTO=new StyleDTO();
        styleDTO.setBaseItemId("3019");
        styleDTOList.add(styleDTO);
        finelineDTO.setStyles(styleDTOList);
        StrongKeyFinelineDTO strongKeyFinelineDTO = new StrongKeyFinelineDTO();
        strongKeyFinelineDTO.setFinelineId(3019L);
        List<StrongKeyStyleDTO> strongKeyStyleDTOS = new ArrayList<>();
        strongKeyFinelineDTO.setStyles(strongKeyStyleDTOS);
        strongKeyDTO.setFineline(strongKeyFinelineDTO);
        changeScopeDTO.setStrongKeys(strongKeyDTO);
        headerDTO.setChangeScope(changeScopeDTO);

        apConsumerService.processMessage(readTextFileAsString("updateChannelOmni"));
        Mockito.verify(midasServiceCall, Mockito.times(1)).invokeMidasApi(any(HeaderDTO.class), any(String.class));
    }

    private String readTextFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/" + fileName + ".txt")));
    }

    @Test
    void getSizeProfileStrategyTest() {
        List<SizeProfileDTO> sizeProfileDTOList = apConsumerService.getLvl3EligibleSizes( 12228, 34L);
        Assertions.assertEquals(1, sizeProfileDTOList.size());
    }

    /**
     * Lvl3 and Fineline are properly populated with size profile data
     * @throws IOException
     */
    @Test
    void sizeProfileFinelineTest() throws IOException {
        Mockito.when(ahsApiProperties.getSizePackReleaseFlag()).thenReturn("true");
        Mockito.when(midasServiceCall.getStoreSizeProfilesV2(any())).thenReturn(storeSizeResponseDto("DEFAULT", 3019));
        Mockito.when(strategyAHSCallService.getSizeDetailsFromAHS(any(), any())).thenReturn(sizeDetailResponse());

        apConsumerService.processMessage(readTextFileAsString("clpMessageFinelineSP"));

        ArgumentCaptor<StrategyPayloadDTO> argument = ArgumentCaptor.forClass(StrategyPayloadDTO.class);
        Mockito.verify(strategyServiceCall).postEventStrategyService(argument.capture(), any());

        StrategyLvl3DTO lvl3StrategyDto = argument.getValue()
              .getLvl1List().get(0)
              .getLvl2List().get(0)
              .getLvl3List().get(0);
        StrategyDTO lvl3Strategy = lvl3StrategyDto.getStrategy();

        //Lvl3 should have a store and online cluster 0
        assertEquals(1, lvl3Strategy.getOnlineSizeClusters().size());
        lvl3Strategy.getOnlineSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(cluster -> {
            assertEquals(1, cluster.getSizeProfiles().size());
            SizeProfileDTO sp = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
        });

        assertEquals(1, lvl3Strategy.getStoreSizeClusters().size());
        lvl3Strategy.getStoreSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(cluster -> {
            assertEquals(1, cluster.getSizeProfiles().size());
            SizeProfileDTO sp = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
        });

        /* Fineline store clusters should be 2.  Cluster 0 should not have a size profile pct.  Clusters >0 should have
            clusters and an associated size profile pct. */
        StrategyFinelinesDTO finelineDTO = lvl3StrategyDto.getLvl4List().get(0).getFinelines().get(0);
        List<SizeCluster> storeClusters = finelineDTO.getStrategy().getStoreSizeClusters();
        assertEquals(2, storeClusters.size());
        storeClusters.forEach(sizeCluster -> {
            SizeProfileDTO sp = sizeCluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());

            if (sizeCluster.getType().getAnalyticsClusterId() == 0)
                assertNull(sp.getSizeProfilePrcnt());
            else {
                assertEquals(75.0, sp.getSizeProfilePrcnt(), 0.0001);
                assertEquals( 75.0, sp.getAdjustedSizeProfile(), 0.0001);
            }

        });

        List<SizeCluster> onlineClusters = finelineDTO.getStrategy().getOnlineSizeClusters();
        assertEquals(1, onlineClusters.size());
        onlineClusters.forEach(cluster -> {
            SizeProfileDTO sizeProfile = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sizeProfile.getSizeDesc());
            assertNull(sizeProfile.getSizeProfilePrcnt());
        });
    }

    @Test
    void sizeProfileLikeFinelineTest() throws IOException {
        Mockito.when(ahsApiProperties.getSizePackReleaseFlag()).thenReturn("true");
        Mockito.when(midasServiceCall.getStoreSizeProfilesV2(any())).thenReturn(storeSizeResponseDto("DEFAULT", 2810));
        Mockito.when(strategyAHSCallService.getSizeDetailsFromAHS(any(), any())).thenReturn(sizeDetailResponse());

        apConsumerService.processMessage(readTextFileAsString("clpMessageLikeFinelineSP"));

        ArgumentCaptor<StrategyPayloadDTO> argument = ArgumentCaptor.forClass(StrategyPayloadDTO.class);
        Mockito.verify(strategyServiceCall).postEventStrategyService(argument.capture(), any());

        StrategyLvl3DTO lvl3StrategyDto = argument.getValue()
                .getLvl1List().get(0)
                .getLvl2List().get(0)
                .getLvl3List().get(0);
        StrategyDTO lvl3Strategy = lvl3StrategyDto.getStrategy();

        //Lvl3 should have a store and online cluster 0
        assertEquals(1, lvl3Strategy.getOnlineSizeClusters().size());
        lvl3Strategy.getOnlineSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(cluster -> {
            assertEquals(1, cluster.getSizeProfiles().size());
            SizeProfileDTO sp = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
        });

        assertEquals(1, lvl3Strategy.getStoreSizeClusters().size());
        lvl3Strategy.getStoreSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(cluster -> {
            assertEquals(1, cluster.getSizeProfiles().size());
            SizeProfileDTO sp = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
        });

        /* Fineline store clusters should be 2.  Cluster 0 should not have a size profile pct.  Clusters >0 should have
            clusters and an associated size profile pct. */
        StrategyFinelinesDTO finelineDTO = lvl3StrategyDto.getLvl4List().get(0).getFinelines().get(0);
        List<SizeCluster> storeClusters = finelineDTO.getStrategy().getStoreSizeClusters();
        assertEquals(2, storeClusters.size());
        storeClusters.forEach(sizeCluster -> {
            SizeProfileDTO sp = sizeCluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());

            if (sizeCluster.getType().getAnalyticsClusterId() == 0)
                assertNull(sp.getSizeProfilePrcnt());
            else {
                assertEquals(75.0, sp.getSizeProfilePrcnt(), 0.0001);
                assertEquals( 75.0, sp.getAdjustedSizeProfile(), 0.0001);
            }

        });

        List<SizeCluster> onlineClusters = finelineDTO.getStrategy().getOnlineSizeClusters();
        assertEquals(1, onlineClusters.size());
        onlineClusters.forEach(cluster -> {
            SizeProfileDTO sizeProfile = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sizeProfile.getSizeDesc());
            assertNull(sizeProfile.getSizeProfilePrcnt());
        });
    }

    @Test
    void sizeProfileStyleTest() throws IOException {
        Mockito.when(ahsApiProperties.getSizePackReleaseFlag()).thenReturn("true");
        Mockito.when(strategyAHSCallService.getSizeDetailsFromAHS(any(), any())).thenReturn(sizeDetailResponse());
        Mockito.when(midasServiceCall.getStoreSizeProfilesV2(any())).thenReturn(storeSizeResponseDto("RED", 3019));
        Mockito.when(midasServiceCall.getOnlineSizeProfilesV2(any())).thenReturn(onlineSizeResponseDto("DEFAULT"));
        apConsumerService.processMessage(readTextFileAsString("clpMessageStyleSP"));

        ArgumentCaptor<StrategyPayloadDTO> argument = ArgumentCaptor.forClass(StrategyPayloadDTO.class);
        Mockito.verify(strategyServiceCall).postEventStrategyService(argument.capture(), any());

        StrategyStyleDTO strategyStyleDto = argument.getValue()
              .getLvl1List().get(0)
              .getLvl2List().get(0)
              .getLvl3List().get(0)
              .getLvl4List().get(0)
              .getFinelines().get(0)
              .getStyles().get(0);
        
        StrategyDTO styleStrategy = strategyStyleDto.getStrategy();

        //Online should have a size profile percentage, but only one cluster 0
        assertEquals(1, styleStrategy.getOnlineSizeClusters().size());
        styleStrategy.getOnlineSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(sizeCluster -> {
            assertEquals((Integer) 0, sizeCluster.getType().getAnalyticsClusterId());
            assertEquals(1, sizeCluster.getSizeProfiles().size());
            SizeProfileDTO sp = sizeCluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
            assertEquals(75.0, sp.getSizeProfilePrcnt(), 0.0001);
            assertEquals(75.0, sp.getAdjustedSizeProfile(), 0.0001);
        });

        //Store should have a cluster 0 with sizes but no size profile percentage
        assertEquals(1, styleStrategy.getStoreSizeClusters().size());
        styleStrategy.getStoreSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(sizeCluster -> {
            assertEquals((Integer) 0, sizeCluster.getType().getAnalyticsClusterId());
            assertEquals(1, sizeCluster.getSizeProfiles().size());
            SizeProfileDTO sp = sizeCluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
            assertNull(sp.getSizeProfilePrcnt());
        });
    }

    @Test
    void sizeProfileCustomerChoiceTest() throws IOException {
        Mockito.when(ahsApiProperties.getSizePackReleaseFlag()).thenReturn("true");
        Mockito.when(strategyAHSCallService.getSizeDetailsFromAHS(any(), any())).thenReturn(sizeDetailResponse());
        Mockito.when(midasServiceCall.getStoreSizeProfilesV2(any())).thenReturn(storeSizeResponseDto("RED", 3019));
        Mockito.when(midasServiceCall.getOnlineSizeProfilesV2(any())).thenReturn(onlineSizeResponseDto("RED"));
        apConsumerService.processMessage(readTextFileAsString("clpMessageCCSP"));

        ArgumentCaptor<StrategyPayloadDTO> argument = ArgumentCaptor.forClass(StrategyPayloadDTO.class);
        Mockito.verify(strategyServiceCall).postEventStrategyService(argument.capture(), any());

        StrategyCustomerChoiceDTO strategyStyleDto = argument.getValue()
              .getLvl1List().get(0)
              .getLvl2List().get(0)
              .getLvl3List().get(0)
              .getLvl4List().get(0)
              .getFinelines().get(0)
              .getStyles().get(0)
              .getCustomerChoices().get(0);

        StrategyDTO customerChoiceStrategy = strategyStyleDto.getStrategy();

        //Online cluster should have size + size percent
        assertEquals(1, customerChoiceStrategy.getOnlineSizeClusters().size());
        customerChoiceStrategy.getOnlineSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(cluster -> {
            assertEquals(1, cluster.getSizeProfiles().size());
            SizeProfileDTO sp = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
            assertNull( sp.getSizeProfilePrcnt());
            assertNull(  sp.getAdjustedSizeProfile());
        });

        //Store cluster should have size but not size percent
        assertEquals(2, customerChoiceStrategy.getStoreSizeClusters().size());
        List<SizeCluster> storeClusters = customerChoiceStrategy.getStoreSizeClusters();
        storeClusters.forEach(sizeCluster -> {
            SizeProfileDTO sp = sizeCluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());

            if (sizeCluster.getType().getAnalyticsClusterId() == 0)
                assertNull(sp.getSizeProfilePrcnt());
            else {
                assertEquals(75.0, sp.getSizeProfilePrcnt(), 0.0001);
                assertEquals( 75.0, sp.getAdjustedSizeProfile(), 0.0001);
            }
        });
    }
    /**
     * A size profile set at the Customer Choice level with a cluster ID that the parent hierarchy didnt have
     * should populate the style and fineline with an empty instance of a cluster with the matching id
     * ex. if CC has clusters {1,2,3} but its parent Style only has {1,2}, then Style should end up with {1,2,3}
     */
    @Test
    void finelineClustersTest() throws IOException {
        StoreSizeProfileDataDTO result = storeSizeProfileData();
        result.getPayload().getResult().getResponse().get(0).setColor_family("DEFAULT");
        result.getPayload().getResult().getResponse().add(storeSizeResponse(2, "DEFAULT", 3019));
        result.getPayload().getResult().getResponse().add(storeSizeResponse(3, "BLUE", 3019));
        Mockito.when(ahsApiProperties.getSizePackReleaseFlag()).thenReturn("true");
        Mockito.when(midasServiceCall.getStoreSizeProfilesV2(any())).thenReturn(result.getPayload().getResult().getResponse());
        apConsumerService.processMessage(readTextFileAsString("clpMessageStoreCCSP"));

        ArgumentCaptor<StrategyPayloadDTO> argument = ArgumentCaptor.forClass(StrategyPayloadDTO.class);
        Mockito.verify(strategyServiceCall).postEventStrategyService(argument.capture(), any());

        StrategyFinelinesDTO strategyFineline = argument.getValue()
              .getLvl1List().get(0)
              .getLvl2List().get(0)
              .getLvl3List().get(0)
              .getLvl4List().get(0)
              .getFinelines().get(0);

        List<SizeCluster> flSizeClusters = strategyFineline.getStrategy().getStoreSizeClusters();
        //fineline dto should have 3 clusters for store, 0, 1, 2
        assertEquals( 3, flSizeClusters.size());
        assertTrue(flSizeClusters.stream().anyMatch(sc -> sc.getType().getAnalyticsClusterId() == 0));
        assertTrue(flSizeClusters.stream().anyMatch(sc -> sc.getType().getAnalyticsClusterId() == 1));
        assertTrue(flSizeClusters.stream().anyMatch(sc -> sc.getType().getAnalyticsClusterId() == 2));

        StrategyStyleDTO strategyStyle = strategyFineline.getStyles().get(0);
        List<SizeCluster> stSizeClusters = strategyStyle.getStrategy().getStoreSizeClusters();
        assertEquals( 3, stSizeClusters.size());
        assertTrue( stSizeClusters.stream().anyMatch(sc -> sc.getType().getAnalyticsClusterId() == 0));
        assertTrue( stSizeClusters.stream().anyMatch(sc -> sc.getType().getAnalyticsClusterId() == 1));
        assertTrue(stSizeClusters.stream().anyMatch(sc -> sc.getType().getAnalyticsClusterId() == 2));
    }

    //Delete event without channel ID should not throw any exception
    @Test
    void deleteEventTest() throws IOException {
        Mockito.when(ahsApiProperties.getSizePackReleaseFlag()).thenReturn("true");
        apConsumerService.processMessage(readTextFileAsString("clpMessageDelete"));
        Mockito.verify(strategyServiceCall, Mockito.times(1)).deleteEventStrategyService(any(), any());
    }

    @Test
    void nullSeasonGetStoreProfilesHandled() {
        apConsumerService.getStoreSizeProfilesV2(null, 1L, 12L, 102L, 123L, Set.of("Black"));
        ArgumentCaptor<StoreSizeProfileRequestDTO> argument = ArgumentCaptor.forClass(StoreSizeProfileRequestDTO.class);
        Mockito.verify(midasServiceCall).getStoreSizeProfilesV2(argument.capture());
        final String expected = "S3";
        final String actual = argument.getValue().getSeason();
        assertEquals(expected, actual, "Default season provided in Store request");
    }

    @Test
    void verifyYearIsExtractedFromStoreProfileDescription() {
        apConsumerService.getStoreSizeProfilesV2("32W2022", 1L,12L, 102L, 123L, Set.of("Black"));
        ArgumentCaptor<StoreSizeProfileRequestDTO> argument = ArgumentCaptor.forClass(StoreSizeProfileRequestDTO.class);
        Mockito.verify(midasServiceCall).getStoreSizeProfilesV2(argument.capture());
        final String expected = "2022";
        final String actual = argument.getValue().getYear();
        assertEquals(expected, actual);
    }

    @Test
    void verifyYearIsExtractedAndIsNullFromStoreProfileDescription() {
        apConsumerService.getStoreSizeProfilesV2(null, 1L, 12L, 102L, 123L, Set.of("Black"));
        ArgumentCaptor<StoreSizeProfileRequestDTO> argument = ArgumentCaptor.forClass(StoreSizeProfileRequestDTO.class);
        Mockito.verify(midasServiceCall).getStoreSizeProfilesV2(argument.capture());
        final String actual = argument.getValue().getYear();
        assertNull(actual);
    }

    @Test
    void verifyYearIsExtractedAndIsNullFromStoreProfileDescriptionWhenDescriptionLengthIsLessThan4Characters() {
        apConsumerService.getStoreSizeProfilesV2("123", 1L, 12L, 102L,  123L, Set.of("Black"));
        ArgumentCaptor<StoreSizeProfileRequestDTO> argument = ArgumentCaptor.forClass(StoreSizeProfileRequestDTO.class);
        Mockito.verify(midasServiceCall).getStoreSizeProfilesV2(argument.capture());
        final String actual = argument.getValue().getYear();
        assertNull(actual);
    }

    @Test
    void nullSeasonGetOnlineProfilesHandled() {
        apConsumerService.getOnlineSizeProfilesV2(null, 1L, Set.of("12345"), Set.of("Black"));
        ArgumentCaptor<OnlineSizeProfileRequestDTO> argument = ArgumentCaptor.forClass(OnlineSizeProfileRequestDTO.class);
        Mockito.verify(midasServiceCall).getOnlineSizeProfilesV2(argument.capture());
        final String expected = "S3";
        final String actual = argument.getValue().getSeason();
        assertEquals(expected, actual, "Default season provided in Online request");
    }

    @Test
    void verifyYearIsExtractedFromOnlineProfileDescription() {
        apConsumerService.getOnlineSizeProfilesV2("32W2022", 1L, Set.of("12345"), Set.of("Black"));
        ArgumentCaptor<OnlineSizeProfileRequestDTO> argument = ArgumentCaptor.forClass(OnlineSizeProfileRequestDTO.class);
        Mockito.verify(midasServiceCall).getOnlineSizeProfilesV2(argument.capture());
        final String expected = "2022";
        final String actual = argument.getValue().getYear();
        assertEquals(expected, actual);
    }

    @Test
    void verifyYearIsExtractedAndIsNullFromOnlineProfileDescription() {
        apConsumerService.getOnlineSizeProfilesV2(null, 1L, Set.of("12345"), Set.of("Black"));
        ArgumentCaptor<OnlineSizeProfileRequestDTO> argument = ArgumentCaptor.forClass(OnlineSizeProfileRequestDTO.class);
        Mockito.verify(midasServiceCall).getOnlineSizeProfilesV2(argument.capture());
        final String actual = argument.getValue().getYear();
        assertNull(actual);
    }

    @Test
    void verifyYearIsExtractedAndIsNullFromOnlineProfileDescriptionWhenDescriptionLengthIsLessThan4Characters() {
        apConsumerService.getOnlineSizeProfilesV2("123", 1L, Set.of("12345"), Set.of("Black"));
        ArgumentCaptor<OnlineSizeProfileRequestDTO> argument = ArgumentCaptor.forClass(OnlineSizeProfileRequestDTO.class);
        Mockito.verify(midasServiceCall).getOnlineSizeProfilesV2(argument.capture());
        final String actual = argument.getValue().getYear();
        assertNull(actual);
    }

    @Test
    void testAcceptedUpdatesForSizePackCallMidas() throws IOException {
        Mockito.when(ahsApiProperties.getSizePackReleaseFlag()).thenReturn("true");
        Mockito.when(midasServiceCall.getStoreSizeProfilesV2(any())).thenReturn(storeSizeResponseDto("default", 3019));
        Mockito.when(strategyAHSCallService.getSizeDetailsFromAHS(any(), any())).thenReturn(sizeDetailResponse());
        apConsumerService.processMessage(readTextFileAsString("updateChannelOmni"));

        ArgumentCaptor<StrategyPayloadDTO> argument = ArgumentCaptor.forClass(StrategyPayloadDTO.class);
        Mockito.verify(strategyServiceCall).postEventStrategyService(argument.capture(), any());

        StrategyLvl3DTO lvl3StrategyDto = argument.getValue()
                .getLvl1List().get(0)
                .getLvl2List().get(0)
                .getLvl3List().get(0);
        StrategyDTO lvl3Strategy = lvl3StrategyDto.getStrategy();

        //Lvl3 should have a store and online cluster 0
        assertEquals(1, lvl3Strategy.getOnlineSizeClusters().size());
        lvl3Strategy.getOnlineSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(cluster -> {
            assertEquals(1, cluster.getSizeProfiles().size());
            SizeProfileDTO sp = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
        });

        assertEquals(1, lvl3Strategy.getStoreSizeClusters().size());
        lvl3Strategy.getStoreSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(cluster -> {
            assertEquals(1, cluster.getSizeProfiles().size());
            SizeProfileDTO sp = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
        });

        /* Fineline store clusters should be 2.  Cluster 0 should not have a size profile pct.  Clusters >0 should have
            clusters and an associated size profile pct. */
        StrategyFinelinesDTO finelineDTO = lvl3StrategyDto.getLvl4List().get(0).getFinelines().get(0);
        List<SizeCluster> storeClusters = finelineDTO.getStrategy().getStoreSizeClusters();
        assertEquals(2, storeClusters.size());
        storeClusters.forEach(sizeCluster -> {
            SizeProfileDTO sp = sizeCluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());

            if (sizeCluster.getType().getAnalyticsClusterId() == 0)
                assertNull(sp.getSizeProfilePrcnt());
            else {
                assertEquals(75.0, sp.getSizeProfilePrcnt(), 0.0001);
                assertEquals( 75.0, sp.getAdjustedSizeProfile(), 0.0001);
            }

        });

        List<SizeCluster> onlineClusters = finelineDTO.getStrategy().getOnlineSizeClusters();
        assertEquals(1, onlineClusters.size());
        onlineClusters.forEach(cluster -> {
            SizeProfileDTO sizeProfile = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sizeProfile.getSizeDesc());
            assertNull(sizeProfile.getSizeProfilePrcnt());
        });

        List<StrategyStyleDTO> styles = finelineDTO.getStyles();
        assertEquals(4, styles.size());
        StrategyStyleDTO strategyStyleDTO = styles.get(0);
        List<SizeCluster> styleStoreClusters = strategyStyleDTO.getStrategy().getStoreSizeClusters();
        //store size cluster must be non null and have one store size cluster.
        assertFalse(CollectionUtils.isEmpty(styleStoreClusters));
        assertEquals(1, styleStoreClusters.size());

        //channel is store at the style, hence no onlineSizeCluster at the style level
        List<SizeCluster> onlineStoreClusters = strategyStyleDTO.getStrategy().getOnlineSizeClusters();
        assertTrue(CollectionUtils.isEmpty(onlineStoreClusters));
        assertEquals(0, onlineStoreClusters.size());
    }
    @Test
    void testAcceptedClusterIDForSizePackCallMidasAddClusterId1IfMidasRespEmpty() throws IOException {
        Mockito.when(ahsApiProperties.getSizePackReleaseFlag()).thenReturn("true");
        Mockito.when(midasServiceCall.getStoreSizeProfilesV2(any())).thenReturn(Collections.emptyList());
        Mockito.when(strategyAHSCallService.getSizeDetailsFromAHS(any(), any())).thenReturn(sizeDetailResponse());
        apConsumerService.processMessage(readTextFileAsString("addClusterId1ForEmptyMidasCallResponse"));

        ArgumentCaptor<StrategyPayloadDTO> argument = ArgumentCaptor.forClass(StrategyPayloadDTO.class);
        Mockito.verify(strategyServiceCall).postEventStrategyService(argument.capture(), any());

        StrategyLvl3DTO lvl3StrategyDto = argument.getValue()
                .getLvl1List().get(0)
                .getLvl2List().get(0)
                .getLvl3List().get(0);
        StrategyDTO lvl3Strategy = lvl3StrategyDto.getStrategy();

        //Lvl3 should have a store and online cluster 0
        assertEquals(1, lvl3Strategy.getOnlineSizeClusters().size());
        lvl3Strategy.getOnlineSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(cluster -> {
            assertEquals(1, cluster.getSizeProfiles().size());
            SizeProfileDTO sp = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
        });

        assertEquals(1, lvl3Strategy.getStoreSizeClusters().size());
        lvl3Strategy.getStoreSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(cluster -> {
            assertEquals(1, cluster.getSizeProfiles().size());
            SizeProfileDTO sp = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
        });

    /* Fineline store clusters should be 2.  Cluster 0 should not have a size profile pct.  Clusters >0 should have
        clusters and an associated size profile pct. */
        StrategyFinelinesDTO finelineDTO = lvl3StrategyDto.getLvl4List().get(0).getFinelines().get(0);
        List<SizeCluster> storeClusters = finelineDTO.getStrategy().getStoreSizeClusters();
        assertEquals(2, storeClusters.size());


        List<SizeCluster> onlineClusters = finelineDTO.getStrategy().getOnlineSizeClusters();
        assertEquals(1, onlineClusters.size());
        onlineClusters.forEach(cluster -> {
            SizeProfileDTO sizeProfile = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sizeProfile.getSizeDesc());
            assertNull(sizeProfile.getSizeProfilePrcnt());
        });

        List<StrategyStyleDTO> styles = finelineDTO.getStyles();
        assertEquals(4, styles.size());
        StrategyStyleDTO strategyStyleDTO = styles.get(0);
        List<SizeCluster> styleStoreClusters = strategyStyleDTO.getStrategy().getStoreSizeClusters();
        //store size cluster must be non null and have one store size cluster.
        assertFalse(CollectionUtils.isEmpty(styleStoreClusters));
        assertEquals(2, styleStoreClusters.size());

        //channel is store at the style, hence no onlineSizeCluster at the style level
        List<SizeCluster> onlineStoreClusters = strategyStyleDTO.getStrategy().getOnlineSizeClusters();
        assertTrue(CollectionUtils.isEmpty(onlineStoreClusters));
        assertEquals(0, onlineStoreClusters.size());
    }

    @Test
    void customerChoiceOmniChannelNoRecommendationsCreatesDefaultCluster1() throws IOException {
        Mockito.when(ahsApiProperties.getSizePackReleaseFlag()).thenReturn("true");
        Mockito.when(strategyAHSCallService.getSizeDetailsFromAHS(any(), any())).thenReturn(sizeDetailResponse());
        apConsumerService.processMessage(readTextFileAsString("addCluster1ForEmptyMidasCallResponseOmni"));

        ArgumentCaptor<StrategyPayloadDTO> argument = ArgumentCaptor.forClass(StrategyPayloadDTO.class);
        Mockito.verify(strategyServiceCall).postEventStrategyService(argument.capture(), any());

        StrategyLvl3DTO lvl3StrategyDto = argument.getValue()
              .getLvl1List().get(0)
              .getLvl2List().get(0)
              .getLvl3List().get(0);

        StrategyFinelinesDTO finelineDTO = lvl3StrategyDto.getLvl4List().get(0).getFinelines().get(0);
        List<SizeCluster> finelineStoreClusters = finelineDTO.getStrategy().getStoreSizeClusters();
        assertEquals( 2, finelineStoreClusters.size(),"Should have 2 clusters");
        assertTrue(finelineStoreClusters.stream().anyMatch(cluster -> cluster.getType().getAnalyticsClusterId().equals(1)), "Should have a cluster 1");
        assertTrue(finelineStoreClusters.stream().anyMatch(cluster -> cluster.getType().getAnalyticsClusterId().equals(0)), "Should have a cluster 0");

        List<StrategyStyleDTO> styles = finelineDTO.getStyles();
        StrategyStyleDTO strategyStyleDTO = styles.get(0);
        List<SizeCluster> styleStoreClusters = strategyStyleDTO.getStrategy().getStoreSizeClusters();
        assertEquals( 2, styleStoreClusters.size(),"Should have 2 clusters");
        assertTrue(styleStoreClusters.stream().anyMatch(cluster -> cluster.getType().getAnalyticsClusterId().equals(1)), "Should have a cluster 1");
        assertTrue(styleStoreClusters.stream().anyMatch(cluster -> cluster.getType().getAnalyticsClusterId().equals(0)), "Should have a cluster 0");
        
        StrategyCustomerChoiceDTO customerChoiceDTO = styles.get(0).getCustomerChoices().get(0);
        List<SizeCluster> ccStoreClusters = customerChoiceDTO.getStrategy().getStoreSizeClusters();
        assertEquals( 2, customerChoiceDTO.getStrategy().getStoreSizeClusters().size(),"Should have 2 clusters");
        assertTrue(ccStoreClusters.stream().anyMatch(cluster -> cluster.getType().getAnalyticsClusterId().equals(1)), "Should have a cluster 1");
        assertTrue(ccStoreClusters.stream().anyMatch(cluster -> cluster.getType().getAnalyticsClusterId().equals(0)), "Should have a cluster 0");
    }



    //This is the testcase for skipping midas api call, hence no storeSizeCluster and onlineSizeCluster set at fineline/style/cc.
    @Test
    void testAcceptedUpdatesForSizePackSkipMidas() throws IOException {
        Mockito.when(ahsApiProperties.getSizePackReleaseFlag()).thenReturn("true");
        Mockito.when(strategyAHSCallService.getSizeDetailsFromAHS(any(), any())).thenReturn(sizeDetailResponse());
        //passing the list of accepted updates as empty, so that Midas call is skipped and sizeClusters are not set.
        Mockito.when(sizePackProperties.getAcceptedSizePackUpdates()).thenReturn(new ArrayList<>());
        apConsumerService.processMessage(readTextFileAsString("updateChannelOmni"));

        ArgumentCaptor<StrategyPayloadDTO> argument = ArgumentCaptor.forClass(StrategyPayloadDTO.class);
        Mockito.verify(strategyServiceCall).postEventStrategyService(argument.capture(), any());

        StrategyLvl3DTO lvl3StrategyDto = argument.getValue()
                .getLvl1List().get(0)
                .getLvl2List().get(0)
                .getLvl3List().get(0);
        StrategyDTO lvl3Strategy = lvl3StrategyDto.getStrategy();

        //Lvl3 should have a store and online cluster 0
        assertEquals(1, lvl3Strategy.getOnlineSizeClusters().size());
        lvl3Strategy.getOnlineSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(cluster -> {
            assertEquals(1, cluster.getSizeProfiles().size());
            SizeProfileDTO sp = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
        });

        assertEquals(1, lvl3Strategy.getStoreSizeClusters().size());
        lvl3Strategy.getStoreSizeClusters().stream().filter(sizeCluster -> sizeCluster.getType().getAnalyticsClusterId() == 0).findFirst().ifPresent(cluster -> {
            assertEquals(1, cluster.getSizeProfiles().size());
            SizeProfileDTO sp = cluster.getSizeProfiles().get(0);
            assertEquals("XS", sp.getSizeDesc());
        });

        StrategyFinelinesDTO finelineDTO = lvl3StrategyDto.getLvl4List().get(0).getFinelines().get(0);
        List<SizeCluster> storeClusters = finelineDTO.getStrategy().getStoreSizeClusters();
        //Since Midas API is not called, the storeSizeCluster is empty list.
        assertTrue(CollectionUtils.isEmpty(storeClusters));

        //Since Midas API is not called, the onlineSizeClusters is empty list.
        List<SizeCluster> onlineClusters = finelineDTO.getStrategy().getOnlineSizeClusters();
        assertTrue(CollectionUtils.isEmpty(onlineClusters));

        List<StrategyStyleDTO> styles = finelineDTO.getStyles();
        assertEquals(4, styles.size());
        StrategyStyleDTO strategyStyleDTO = styles.get(0);
        List<SizeCluster> styleStoreClusters = strategyStyleDTO.getStrategy().getStoreSizeClusters();
        //Since Midas API is not called, the storeSizeCluster is empty list.
        assertTrue(CollectionUtils.isEmpty(styleStoreClusters));

        List<SizeCluster> onlineStoreClusters = strategyStyleDTO.getStrategy().getOnlineSizeClusters();
        //Since Midas API is not called, the onlineSizeClusters is empty list.
        assertTrue(CollectionUtils.isEmpty(onlineStoreClusters));
    }

    @Test
    void testAddUnmatchedStyleStoreClustersToFineline(){
        StrategyDTO strategyDTOForFineline = new StrategyDTO();
        StrategyDTO strategyDTOForStyle1 = new StrategyDTO();
        StrategyDTO strategyDTOForStyle2 = new StrategyDTO();

        List<SizeProfileDTO> sizeProfiles = new ArrayList<>();
        SizeProfileDTO sizeProfileDTO = getSizeProfileDTO(30.0,"0XP",246,20.0,1);
        sizeProfiles.add(sizeProfileDTO);
        List<SizeProfileDTO> sizeProfiles1 = new ArrayList<>();
        SizeProfileDTO sizeProfileDTO1 = getSizeProfileDTO(40.0,"1XP",247,30.0,1);
        sizeProfiles1.add(sizeProfileDTO1);

        List<SizeCluster> fl_storeSizeClusters = new ArrayList<>();
        SizeCluster fl_storeSizeCluster = new SizeCluster();
        ClusterType fl_type = getClusterType(0,"all");
        fl_storeSizeCluster.setType(fl_type);
        fl_storeSizeCluster.setSizeProfiles(sizeProfiles);
        fl_storeSizeClusters.add(fl_storeSizeCluster);
        strategyDTOForFineline.setStoreSizeClusters(fl_storeSizeClusters);

        List<SizeCluster> style_storeSizeClusters1 = new ArrayList<>();
        SizeCluster style_storeSizeCluster1 = new SizeCluster();
        ClusterType style_type = getClusterType(0,"all");
        style_storeSizeCluster1.setType(style_type);
        style_storeSizeCluster1.setSizeProfiles(sizeProfiles);
        style_storeSizeClusters1.add(style_storeSizeCluster1);

        SizeCluster style_storeSizeCluster2 = new SizeCluster();
        ClusterType style_type1 = getClusterType(1,"default");
        style_storeSizeCluster2.setType(style_type1);
        style_storeSizeCluster2.setSizeProfiles(sizeProfiles1);
        style_storeSizeClusters1.add(style_storeSizeCluster2);
        strategyDTOForStyle1.setStoreSizeClusters(style_storeSizeClusters1);

        List<SizeCluster> style_storeSizeClusters2 = new ArrayList<>();
        SizeCluster style2_storeSizeCluster2 = new SizeCluster();
        style2_storeSizeCluster2.setType(style_type);
        style2_storeSizeCluster2.setSizeProfiles(sizeProfiles);
        style_storeSizeClusters2.add(style2_storeSizeCluster2);
        strategyDTOForStyle2.setStoreSizeClusters(style_storeSizeClusters2);

        StrategyFinelinesDTO strategyFinelineDto = new StrategyFinelinesDTO();
        strategyFinelineDto.setFinelineNbr(3019l);
        strategyFinelineDto.setChannel("Omni");
        strategyFinelineDto.setStrategy(strategyDTOForFineline);
        List<StrategyStyleDTO> styles = new ArrayList<>();
        StrategyStyleDTO style1 = getStrategyStyleDTO("6419_3019_3_24_001","Omni" ,"xyz_style");
        style1.setStrategy(strategyDTOForStyle1);

        StrategyStyleDTO style2 = getStrategyStyleDTO("6419_3019_3_24_002", "Omni", "xyz_style2");
        style2.setStrategy(strategyDTOForStyle2);

        List<StrategyCustomerChoiceDTO> strategyCustomerChoiceDTOS1 = new ArrayList<>();
        StrategyCustomerChoiceDTO strategyCustomerChoiceDTO1 = getStrategyCustomerChoiceDTO("6419_3019_3_24_001_001","Omni", strategyDTOForStyle1);
        strategyCustomerChoiceDTOS1.add(strategyCustomerChoiceDTO1);
        style1.setCustomerChoices(strategyCustomerChoiceDTOS1);

        List<StrategyCustomerChoiceDTO> strategyCustomerChoiceDTOS2 = new ArrayList<>();
        StrategyCustomerChoiceDTO strategyCustomerChoiceDTO2 = getStrategyCustomerChoiceDTO("6419_3019_3_24_002_001", "Omni",strategyDTOForStyle2);
        strategyCustomerChoiceDTOS2.add(strategyCustomerChoiceDTO2);
        style2.setCustomerChoices(strategyCustomerChoiceDTOS2);

        styles.add(style1);
        styles.add(style2);
        strategyFinelineDto.setStyles(styles);

        StrategyDTO lvl3Strategy = new StrategyDTO();
        List<SizeCluster> lvl3_storeSizeClusters = new ArrayList<>();
        lvl3_storeSizeClusters.add(style_storeSizeCluster1);
        lvl3_storeSizeClusters.add(style_storeSizeCluster2);
        lvl3Strategy.setStoreSizeClusters(lvl3_storeSizeClusters);

        apConsumerService.addUnmatchedStyleStoreClustersToFineline(strategyFinelineDto,strategyDTOForFineline,lvl3Strategy);
        assertEquals(2,strategyDTOForFineline.getStoreSizeClusters().size());
        assertEquals("0XP",strategyDTOForFineline.getStoreSizeClusters().get(1).getSizeProfiles().get(0).getSizeDesc());
    }

    @Test
    void testAddUnmatchedCcStoreClustersToStyle(){
        StrategyDTO strategyDTOForStyle = new StrategyDTO();
        StrategyDTO strategyDTOForCC1 = new StrategyDTO();
        StrategyDTO strategyDTOForCC2 = new StrategyDTO();

        List<SizeProfileDTO> sizeProfiles = new ArrayList<>();
        SizeProfileDTO sizeProfileDTO = getSizeProfileDTO(30.0,"0XP",246,20.0,1);
        sizeProfiles.add(sizeProfileDTO);
        List<SizeProfileDTO> sizeProfiles1 = new ArrayList<>();
        SizeProfileDTO sizeProfileDTO1 = getSizeProfileDTO(40.0,"1XP",247,30.0,1);
        sizeProfiles1.add(sizeProfileDTO1);

        List<SizeCluster> style_storeSizeClusters = new ArrayList<>();
        SizeCluster style_storeSizeCluster = new SizeCluster();
        ClusterType style_type = getClusterType(0,"all");
        style_storeSizeCluster.setType(style_type);
        style_storeSizeCluster.setSizeProfiles(sizeProfiles);
        style_storeSizeClusters.add(style_storeSizeCluster);
        strategyDTOForStyle.setStoreSizeClusters(style_storeSizeClusters);

        List<SizeCluster> cc_storeSizeClusters1 = new ArrayList<>();
        SizeCluster cc_storeSizeCluster1 = new SizeCluster();
        ClusterType cc_type = getClusterType(0,"all");
        cc_storeSizeCluster1.setType(cc_type);
        cc_storeSizeCluster1.setSizeProfiles(sizeProfiles);
        cc_storeSizeClusters1.add(cc_storeSizeCluster1);
        strategyDTOForCC1.setStoreSizeClusters(cc_storeSizeClusters1);

        List<SizeCluster> cc_storeSizeClusters2 = new ArrayList<>();
        SizeCluster cc_storeSizeCluster2 = new SizeCluster();
        ClusterType cc_type1 = getClusterType(1,"default");
        cc_storeSizeCluster2.setType(cc_type1);
        cc_storeSizeCluster2.setSizeProfiles(sizeProfiles1);
        cc_storeSizeClusters2.add(cc_storeSizeCluster2);
        strategyDTOForCC2.setStoreSizeClusters(cc_storeSizeClusters2);


        List<StrategyStyleDTO> styles = new ArrayList<>();
        StrategyStyleDTO style = getStrategyStyleDTO("6419_3019_3_24_001", "Omni","xyz_style");
        style.setStrategy(strategyDTOForStyle);

        List<StrategyCustomerChoiceDTO> strategyCustomerChoiceDTOS = new ArrayList<>();
        StrategyCustomerChoiceDTO strategyCustomerChoiceDTO1 = getStrategyCustomerChoiceDTO("6419_3019_3_24_001_001", "Omni",strategyDTOForCC1);
        strategyCustomerChoiceDTOS.add(strategyCustomerChoiceDTO1);

        StrategyCustomerChoiceDTO strategyCustomerChoiceDTO2 = getStrategyCustomerChoiceDTO("6419_3019_3_24_002_001", "Omni",strategyDTOForCC2);
        strategyCustomerChoiceDTOS.add(strategyCustomerChoiceDTO2);
        style.setCustomerChoices(strategyCustomerChoiceDTOS);
        styles.add(style);

        StrategyDTO lvl3Strategy = new StrategyDTO();
        List<SizeCluster> lvl3_storeSizeClusters = new ArrayList<>();
        lvl3_storeSizeClusters.add(cc_storeSizeCluster1);
        lvl3_storeSizeClusters.add(cc_storeSizeCluster2);
        lvl3Strategy.setStoreSizeClusters(lvl3_storeSizeClusters);

        apConsumerService.addUnmatchedCcStoreClustersToStyle(style,strategyDTOForStyle,lvl3Strategy);
        assertEquals(2,strategyDTOForStyle.getStoreSizeClusters().size());
        assertEquals("0XP",strategyDTOForStyle.getStoreSizeClusters().get(1).getSizeProfiles().get(0).getSizeDesc());
    }

    @Test
    void colorFamiliesUpperCasedWhenMidasCalledStore() {
        ArgumentCaptor<StoreSizeProfileRequestDTO> argument = ArgumentCaptor.forClass(StoreSizeProfileRequestDTO.class);
        apConsumerService.getStoreSizeProfilesV2("test plan", 1L, 12L, 102L, 123L, Set.of("Red","Blue","default"));
        Mockito.verify(midasServiceCall).getStoreSizeProfilesV2(argument.capture());
        Set<String> expected = Set.of("RED", "BLUE", "DEFAULT");
        Set<String> actual = argument.getValue().getColorFamilies();
        assertEquals(expected, actual);
    }

    @Test
    void colorFamiliesUpperCasedWhenMidasCalledOnline() {
        ArgumentCaptor<OnlineSizeProfileRequestDTO> argument = ArgumentCaptor.forClass(OnlineSizeProfileRequestDTO.class);
        apConsumerService.getOnlineSizeProfilesV2("test plan", 1L, Set.of("12345"), Set.of("Red","Blue","default"));
        Mockito.verify(midasServiceCall).getOnlineSizeProfilesV2(argument.capture());
        Set<String> expected = Set.of("RED", "BLUE", "DEFAULT");
        Set<String> actual = argument.getValue().getColorFamilies();
        assertEquals(expected, actual);
    }

    private StrategyCustomerChoiceDTO getStrategyCustomerChoiceDTO(String ccId, String channel,StrategyDTO strategyDTOForCC1) {
        StrategyCustomerChoiceDTO strategyCustomerChoiceDTO1 = new StrategyCustomerChoiceDTO();
        strategyCustomerChoiceDTO1.setCcId(ccId);
        strategyCustomerChoiceDTO1.setChannel(channel);
        strategyCustomerChoiceDTO1.setStrategy(strategyDTOForCC1);
        return strategyCustomerChoiceDTO1;
    }

    private StrategyStyleDTO getStrategyStyleDTO(String styleNbr, String channel, String xyz_style) {
        StrategyStyleDTO style = new StrategyStyleDTO();
        style.setStyleNbr(styleNbr);
        style.setChannel(channel);
        style.setAltStyleDesc(xyz_style);
        return style;
    }

    private ClusterType getClusterType(int analyticsClusterId , String analyticsClusterDesc ) {
        ClusterType type = new ClusterType();
        type.setAnalyticsClusterId(analyticsClusterId);
        type.setAnalyticsClusterDesc(analyticsClusterDesc);
        return type;
    }

    private SizeProfileDTO getSizeProfileDTO(double AdjustedSizeProfile, String sizeDesc , int AHSId , double prcnt, int isEligible) {
        SizeProfileDTO sizeProfileDTO = new SizeProfileDTO();
        sizeProfileDTO.setAdjustedSizeProfile(AdjustedSizeProfile);
        sizeProfileDTO.setSizeDesc(sizeDesc);
        sizeProfileDTO.setAhsSizeId(AHSId);
        sizeProfileDTO.setSizeProfilePrcnt(prcnt);
        sizeProfileDTO.setIsEligible(isEligible);
        return sizeProfileDTO;
    }

    private StoreSizeProfileDataDTO storeSizeProfileData() {
        StoreSizeProfileDataDTO response = new StoreSizeProfileDataDTO();
        response.setPayload(storeSizeProfilePayload());
        return response;
    }

    private StoreSizeProfilePayloadDTO storeSizeProfilePayload() {
        StoreSizeProfilePayloadDTO response = new StoreSizeProfilePayloadDTO();
        response.setResult(storeSizeProfileResult());
        return response;
    }

    private StoreSizeProfileResultDTO storeSizeProfileResult() {
        StoreSizeProfileResultDTO response = new StoreSizeProfileResultDTO();
        response.setResponse(storeSizeResponseDto("Red", 3019));
        return response;
    }

    private List<StoreSizeResponseDTO> storeSizeResponseDto(String colorFamily, int finelineNbr) {
        List<StoreSizeResponseDTO> response = new ArrayList<>();
        response.add(storeSizeResponse(1, colorFamily, finelineNbr));
        return response;
    }

    private StoreSizeResponseDTO storeSizeResponse(int clusterId, String colorFamily, int finelineNbr) {
        StoreSizeResponseDTO ssResponse = new StoreSizeResponseDTO();
        ssResponse.setSeason("S3");
        ssResponse.setDept_catg_nbr(12234L);
        ssResponse.setDept_subcatg_nbr(31517L);
        ssResponse.setFineline_nbr(finelineNbr);
        ssResponse.setColor_family(colorFamily);
        ssResponse.setSize_desc("XS");
        ssResponse.setSize_profile(75.0);
        ssResponse.setCluster_id(clusterId);
        return ssResponse;
    }


    private OnlineSizeProfileDataDTO onlineSizeProfileData() {
        OnlineSizeProfileDataDTO response = new OnlineSizeProfileDataDTO();
        response.setPayload(onlineSizeProfilePayload());
        return response;
    }

    private OnlineSizeProfilePayloadDTO onlineSizeProfilePayload() {
        OnlineSizeProfilePayloadDTO response = new OnlineSizeProfilePayloadDTO();
        response.setResult(onlineSizeProfileResult());
        return response;
    }

    private OnlineSizeProfileResultDTO onlineSizeProfileResult() {
        OnlineSizeProfileResultDTO response = new OnlineSizeProfileResultDTO();
        response.setResponse(onlineSizeResponseDto("Red"));
        return response;
    }

    private List<OnlineSizeResponseDTO> onlineSizeResponseDto(String colorFamily) {
        List<OnlineSizeResponseDTO> response = new ArrayList<>();
        OnlineSizeResponseDTO ssResponse = new OnlineSizeResponseDTO();
        ssResponse.setBase_itm_id(123l);
        ssResponse.setSeason("S3");
        ssResponse.setColor_family(colorFamily);
        ssResponse.setSize_desc("XS");
        ssResponse.setSize_profile(75.0);
        ssResponse.setCluster_id(1);
        ssResponse.setBase_itm_id(2584234L);
        response.add(ssResponse);
        return response;
    }

    private SizeDetailResponseDTO[] sizeDetailResponse() {
        SizeDetailResponseDTO[] sizeDetails = new SizeDetailResponseDTO[1];
        sizeDetails[0] = new SizeDetailResponseDTO(254, "XS", new HashMap<>());
        return sizeDetails;
    }

}
