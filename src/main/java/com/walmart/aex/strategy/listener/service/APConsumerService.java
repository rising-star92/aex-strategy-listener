package com.walmart.aex.strategy.listener.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.aex.strategy.listener.dto.*;
import com.walmart.aex.strategy.listener.dto.midas.FinelineRankMetricsDTO;
import com.walmart.aex.strategy.listener.dto.midas.FinelineRankMetricsPayloadDTO;
import com.walmart.aex.strategy.listener.dto.midas.ResultDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.OnlineSizeResponseDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.SizeProfileResponse;
import com.walmart.aex.strategy.listener.dto.midas.response.StoreSizeResponseDTO;
import com.walmart.aex.strategy.listener.dto.strategy.request.*;
import com.walmart.aex.strategy.listener.enums.EventType;
import com.walmart.aex.strategy.listener.enums.OrderPrefType;
import com.walmart.aex.strategy.listener.properties.*;
import io.strati.ccm.utils.client.annotation.ManagedConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.walmart.aex.strategy.listener.utils.StrategyListenerConstants.DEFAULT_IS_ELIGIBLE;


@Service
@Slf4j
public class APConsumerService {
    private static final int DEFAULT_CLUSTER_ID = 0;
    private static final int DEFAULT_CLUSTER_ID_1 = 1;
    private static final String DEFAULT_CLUSTER_DESC = "all";
    private static final String DEFAULT_COLOR = "DEFAULT";
    private static final String OMNI = "Omni";
    private static final String ONLINE = "Online";
    private static final String STORE = "Store";

    //TODO This is a stopgap until LP events properly populate planDesc in all events
    private static final String DEFAULT_SEASON = "S3";

    @Autowired
    private MidasServiceCall midasServiceCall;
    @Autowired
    private StrategyServiceCall strategyServiceCall;
    @Autowired
    private StrategyAHSCallService strategyAHSCallService;
    @ManagedConfiguration
    private KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;
    @ManagedConfiguration
    private StrategyServiceProperties strategyServiceProperties;
    @ManagedConfiguration
    private AHSApiProperties ahsApiProperties;
    @ManagedConfiguration
    private SizePackProperties sizePackProperties;
    @ManagedConfiguration
    private FeatureConfigProperties featureConfigProperties;

    public APConsumerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void processMessage(String message) throws JsonProcessingException {
        log.info("Processing message");
        CLPMessageDTO messageDTO = objectMapper.readValue(message, CLPMessageDTO.class);
        if (messageDTO.getHeaders().getType().equalsIgnoreCase(EventType.CREATE.name()) ||
                messageDTO.getHeaders().getType().equalsIgnoreCase(EventType.INITIAL_LOAD.name())) {
            String eventType = messageDTO.getHeaders().getType();
            handleCreateEvent(messageDTO.getHeaders(), messageDTO.getPayload(), eventType);
        } else if (messageDTO.getHeaders().getType().equalsIgnoreCase(EventType.UPDATE.name())) {
            handleUpdateEvent(messageDTO);
        } else if (messageDTO.getHeaders().getType().equalsIgnoreCase(EventType.DELETE.name())) {
            handleDeleteEvent(messageDTO);
        } else {
            log.info("Payload not processed and discarded!");
        }
    }

    private void handleCreateEvent(HeaderDTO headerDto, PayloadDTO payloadDTO, String eventType) throws JsonProcessingException {
        createStrategyPayload(headerDto, payloadDTO,eventType);
    }

    private void handleUpdateEvent(CLPMessageDTO messageDTO) throws JsonProcessingException {
        createStrategyPayload(messageDTO.getHeaders(), messageDTO.getPayload(), EventType.UPDATE.name());
    }

    private void handleDeleteEvent(CLPMessageDTO messageDTO) throws JsonProcessingException {
        createStrategyPayload(messageDTO.getHeaders(), messageDTO.getPayload(), EventType.DELETE.name());
    }

    private void createStrategyPayload(HeaderDTO headerDTO, PayloadDTO payloadDTO, String eventType) throws JsonProcessingException {
        if (payloadDTO != null) {
            StrategyPayloadDTO strategyPayloadDTO = new StrategyPayloadDTO();
            strategyPayloadDTO.setPlanId(payloadDTO.getPlanId());
            strategyPayloadDTO.setPlanDesc(payloadDTO.getPlanDesc());
            strategyPayloadDTO.setLvl0Nbr(payloadDTO.getLvl0Nbr());

            StrongKeyDTO strongKeyDTO = Optional.ofNullable(headerDTO.getChangeScope())
                    .map(ChangeScopeDTO::getStrongKeys)
                    .orElse(new StrongKeyDTO());
            strongKeyDTO.setPlanId(payloadDTO.getPlanId());

            strategyPayloadDTO.setLvl0Name(strongKeyDTO.getLvl0GenDesc1());
            strategyPayloadDTO.setLvl1List(setLvl1Strategy(headerDTO, strongKeyDTO, eventType, payloadDTO));
            log.info("Strategy Payload: {}", objectMapper.writeValueAsString(strategyPayloadDTO));
            postStrategy(eventType, strategyPayloadDTO, strongKeyDTO);
        }
    }

    private List<StrategyLvl1DTO> setLvl1Strategy(HeaderDTO headerDTO, StrongKeyDTO strongKeyDTO, String eventType, PayloadDTO payloadDTO) {
        List<StrategyLvl1DTO> strategyLvl1DTOList = new ArrayList<>();
        StrategyLvl1DTO strategyLvl1DTO = new StrategyLvl1DTO();
        strategyLvl1DTO.setLvl1Nbr(payloadDTO.getLvl1Nbr());
        strategyLvl1DTO.setLvl1Name(strongKeyDTO.getLvl1GenDesc1());
        strategyLvl1DTO.setLvl2List(setLvl2Strategy(headerDTO, strongKeyDTO, eventType, payloadDTO));
        strategyLvl1DTOList.add(strategyLvl1DTO);
        return strategyLvl1DTOList;
    }

    private List<StrategyLvl2DTO> setLvl2Strategy(HeaderDTO headerDTO, StrongKeyDTO strongKeyDTO, String eventType, PayloadDTO payloadDTO) {
        List<StrategyLvl2DTO> strategyLvl2DTOList = new ArrayList<>();
        StrategyLvl2DTO strategyLvl2DTO = new StrategyLvl2DTO();
        strategyLvl2DTO.setLvl2Nbr(payloadDTO.getLvl2Nbr());
        strategyLvl2DTO.setLvl2Name(strongKeyDTO.getLvl2GenDesc1());
        strategyLvl2DTO.setLvl3List(setLvl3Strategy(headerDTO, eventType, Optional.ofNullable(payloadDTO.getLvl3()).orElse(new Lvl3DTO()), payloadDTO));
        strategyLvl2DTOList.add(strategyLvl2DTO);
        return strategyLvl2DTOList;
    }

    private List<StrategyLvl3DTO> setLvl3Strategy(HeaderDTO headerDTO, String eventType, Lvl3DTO lvl3DTO, PayloadDTO payloadDTO) {
        List<StrategyLvl3DTO> strategyLvl3DTOList = new ArrayList<>();
        StrategyLvl3DTO strategyLvl3DTO = new StrategyLvl3DTO();
        strategyLvl3DTO.setLvl3Nbr(lvl3DTO.getLvl3Nbr());
        strategyLvl3DTO.setLvl3Name(lvl3DTO.getLvl3Name());

        // Added this part for getting SizeProfile Strategy Cluster when event is CREATE --SY size filter
        if (eventType.equalsIgnoreCase(EventType.CREATE.name()) || (eventType.equalsIgnoreCase(EventType.UPDATE.name())) ||
                (isInitialLoadAndKafkaSizePackEnabled(eventType)) && isSizeStrategyEnabled()) {
            StrategyDTO lvl3Strategy = getSizeProfileStrategyDTO(lvl3DTO.getLvl3Nbr(), payloadDTO.getLvl1Nbr());
            strategyLvl3DTO.setStrategy(lvl3Strategy);
        }

        strategyLvl3DTO.setLvl4List(setLvl4Strategy(headerDTO, eventType, Optional.ofNullable(lvl3DTO.getLvl4()).orElse(new Lvl4DTO()), payloadDTO, strategyLvl3DTO.getStrategy()));
        strategyLvl3DTOList.add(strategyLvl3DTO);
        return strategyLvl3DTOList;
    }

    private List<StrategyLvl4DTO> setLvl4Strategy(HeaderDTO headerDTO, String eventType, Lvl4DTO lvl4DTO, PayloadDTO payloadDTO, StrategyDTO lvl3Strategy) {
        List<StrategyLvl4DTO> strategyLvl4DTOList = new ArrayList<>();
        StrategyLvl4DTO strategyLvl4DTO = new StrategyLvl4DTO();
        strategyLvl4DTO.setLvl4Nbr(lvl4DTO.getLvl4Nbr());
        strategyLvl4DTO.setLvl4Name(lvl4DTO.getLvl4Name());
        strategyLvl4DTO.setFinelines(setFinelineStrategy(headerDTO, eventType, Optional.ofNullable(lvl4DTO.getFinelines()).orElse(new FinelinePayloadDTO()), payloadDTO, lvl3Strategy));
        strategyLvl4DTOList.add(strategyLvl4DTO);
        return strategyLvl4DTOList;
    }

    private List<StrategyFinelinesDTO> setFinelineStrategy(HeaderDTO headerDTO, String eventType, FinelinePayloadDTO finelines, PayloadDTO payloadDTO, StrategyDTO lvl3Strategy) {
        List<StrategyFinelinesDTO> strategyFinelinesDTOList = new ArrayList<>();
        strategyFinelinesDTOList.add(getFinelineStrategy(headerDTO, eventType, finelines, payloadDTO, lvl3Strategy));
        return strategyFinelinesDTOList;
    }

    private void postStrategy(String eventType, StrategyPayloadDTO strategyPayloadDTO, StrongKeyDTO strongKeyDTO) throws JsonProcessingException {
        if (eventType.equalsIgnoreCase(EventType.CREATE.name()) ||
                eventType.equalsIgnoreCase(EventType.INITIAL_LOAD.name())) {
            String response = strategyServiceCall.postEventStrategyService(strategyPayloadDTO, HttpMethod.POST);
            log.info("Post create event : {}", response);
        } else if (eventType.equalsIgnoreCase(EventType.UPDATE.name())) {
            String response = strategyServiceCall.postEventStrategyService(strategyPayloadDTO, HttpMethod.PUT);
            log.info("Post update event : {}", response);
        } else if (eventType.equalsIgnoreCase(EventType.DELETE.name())) {
            StrategyDeletePayloadDTO strategyDeletePayloadDTO = new StrategyDeletePayloadDTO();
            strategyDeletePayloadDTO.setPlanStrategyDto(strategyPayloadDTO);
            strategyDeletePayloadDTO.setStrongKey(getStrongKey(strongKeyDTO,strategyPayloadDTO.getPlanDesc()));
            log.info("Strategy Delete Payload: {}", objectMapper.writeValueAsString(strategyDeletePayloadDTO));
            String response = strategyServiceCall.deleteEventStrategyService(strategyDeletePayloadDTO, HttpMethod.DELETE);
            log.info("Post delete event : {}", response);
        }
    }

    private StrategyStrongKeyDTO getStrongKey(StrongKeyDTO strongKeyDTO, String planDesc) {
        StrategyStrongKeyDTO strategyStrongKeyDTO = new StrategyStrongKeyDTO();
        strategyStrongKeyDTO.setPlanId(strongKeyDTO.getPlanId());
        strategyStrongKeyDTO.setPlanDesc(planDesc);
        strategyStrongKeyDTO.setLvl0Nbr(strongKeyDTO.getLvl0Nbr());
        strategyStrongKeyDTO.setLvl1Nbr(strongKeyDTO.getLvl1Nbr());
        strategyStrongKeyDTO.setLvl2Nbr(strongKeyDTO.getLvl2Nbr());
        strategyStrongKeyDTO.setLvl3Nbr(strongKeyDTO.getLvl3Nbr());
        strategyStrongKeyDTO.setLvl4Nbr(strongKeyDTO.getLvl4Nbr());

        StrategyFinelinesDTO strategyFinelinesDTO = new StrategyFinelinesDTO();
        StrongKeyFinelineDTO strongKeyFinelineDTO = Optional.ofNullable(strongKeyDTO.getFineline()).orElse(new StrongKeyFinelineDTO());
        strategyFinelinesDTO.setFinelineNbr(strongKeyFinelineDTO.getFinelineId());
        List<StrategyStyleDTO> strategyStyleDTOS = new ArrayList<>();

        if (!CollectionUtils.isEmpty(strongKeyFinelineDTO.getStyles())) {
            strongKeyFinelineDTO.getStyles().forEach(strongKeyStyleDTO -> {
                StrategyStyleDTO strategyStyleDTO = new StrategyStyleDTO();
                strategyStyleDTO.setStyleNbr(strongKeyStyleDTO.getStyleId());
                List<StrategyCustomerChoiceDTO> strategyCustomerChoiceDTOS = new ArrayList<>();
                if (!CollectionUtils.isEmpty(strongKeyStyleDTO.getCcIds())) {
                    strongKeyStyleDTO.getCcIds().forEach(ccId -> {
                        StrategyCustomerChoiceDTO strategyCustomerChoiceDTO = new StrategyCustomerChoiceDTO();
                        strategyCustomerChoiceDTO.setCcId(ccId);
                        strategyCustomerChoiceDTOS.add(strategyCustomerChoiceDTO);
                    });
                }
                strategyStyleDTO.setCustomerChoices(strategyCustomerChoiceDTOS);
                strategyStyleDTOS.add(strategyStyleDTO);
            });
            strategyFinelinesDTO.setStyles(strategyStyleDTOS);
        }
        strategyStrongKeyDTO.setFineline(strategyFinelinesDTO);
        return strategyStrongKeyDTO;
    }

    private boolean isKafkaConsumerSizePackFlagEnabled() {
        return Boolean.parseBoolean(kafkaProperties.getKafkaConsumerSizePackFlag());
    }
    private StrategyFinelinesDTO getFinelineStrategy(HeaderDTO headerDTO, String eventType, FinelinePayloadDTO finelinePayloadDTO, PayloadDTO payloadDTO, StrategyDTO lvl3Strategy) {
        StrategyFinelinesDTO strategyFinelinesDTO = new StrategyFinelinesDTO();
        strategyFinelinesDTO.setFinelineNbr(finelinePayloadDTO.getFinelineId());
        strategyFinelinesDTO.setFinelineName(finelinePayloadDTO.getFinelineName());
        strategyFinelinesDTO.setAltFinelineName(finelinePayloadDTO.getAltFinelineName());
        strategyFinelinesDTO.setChannel(finelinePayloadDTO.getChannel());

        TypeSpecificDTO typeSpecificDTO = Optional.ofNullable(finelinePayloadDTO.getMetrics())
                .map(MetricsDTO::getCurrent)
                .map(CurrentMetricsDTO::getStore)
                .map(StoreMetricsDTO::getProductAttributes)
                .map(ProductAttributesDTO::getTypeSpecific)
                .orElse(new TypeSpecificDTO());

        strategyFinelinesDTO.setTraitChoice(typeSpecificDTO.getTraitChoice());
        strategyFinelinesDTO.setOutFitting(typeSpecificDTO.getOutFitting());
        strategyFinelinesDTO.setBrands(typeSpecificDTO.getBrands());
        strategyFinelinesDTO.setProductDimensions(typeSpecificDTO.getProductDimensions());

        strategyFinelinesDTO.setStrategy(getStrategyDTO(headerDTO, eventType, finelinePayloadDTO, typeSpecificDTO, payloadDTO.getPlanDesc()));

        //get and set the updatedUpdates for size and pack once, and reuse it all the places.
        boolean isSizePackStrategyEnabled = isSizePackStrategyEnabled(eventType, headerDTO);
        List<StrategyStyleDTO> strategyStyleDTOList = new ArrayList<>();
        List<StoreSizeResponseDTO> storeSizeProfiles = Collections.emptyList();
        List<OnlineSizeResponseDTO> onlineSizeProfiles = Collections.emptyList();
        final List<StyleDTO> styleDTOs = Optional.ofNullable(finelinePayloadDTO.getStyles()).orElse(Collections.emptyList());

        if (isSizePackStrategyEnabled || isInitialLoadAndKafkaSizePackEnabled(eventType)) {
            Set<String> storeColorFamilies = styleDTOs.stream()
                    .map(StyleDTO::getCustomerChoices)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .map(customerChoiceDTO -> getColorFamilyOrDefault(getCurrentMetricsDTO(customerChoiceDTO).getStore().getProductAttributes()))
                    .collect(Collectors.toSet());
            storeColorFamilies.add(DEFAULT_COLOR);
            Set<String> onlineColorFamilies = styleDTOs.stream()
                    .map(StyleDTO::getCustomerChoices)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .map(customerChoiceDTO -> getColorFamilyOrDefault(getCurrentMetricsDTO(customerChoiceDTO).getOnline().getProductAttributes()))
                    .collect(Collectors.toSet());
            onlineColorFamilies.add(DEFAULT_COLOR);

            Long finelineNbr = finelinePayloadDTO.getFinelineId();
            Long lvl1Nbr = payloadDTO.getLvl1Nbr();
            Long lvl3Nbr = payloadDTO.getLvl3Nbr();
            Long lvl4Nbr = payloadDTO.getLvl4Nbr();

            if (featureConfigProperties.isLikeFinelineFeatureFlag() && ObjectUtils.allNotNull(finelinePayloadDTO.getLikeAssociation().getId(), finelinePayloadDTO.getLikeAssociation().getLvl1Nbr())) {
                finelineNbr = Long.valueOf(finelinePayloadDTO.getLikeAssociation().getId());
                lvl1Nbr = finelinePayloadDTO.getLikeAssociation().getLvl1Nbr().longValue();
                lvl3Nbr = finelinePayloadDTO.getLikeAssociation().getLvl3Nbr().longValue();
                lvl4Nbr = finelinePayloadDTO.getLikeAssociation().getLvl4Nbr().longValue();
            }

            storeSizeProfiles = getStoreSizeProfilesV2(payloadDTO.getPlanDesc(), lvl1Nbr, lvl3Nbr, lvl4Nbr,
                    finelineNbr, storeColorFamilies);

            Set<String> baseItemIds = styleDTOs.stream()
                  .map(StyleDTO::getBaseItemId)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toSet());
            if (!baseItemIds.isEmpty())
                onlineSizeProfiles = getOnlineSizeProfilesV2(payloadDTO.getPlanDesc(), payloadDTO.getLvl1Nbr(),
                      baseItemIds, onlineColorFamilies);
        }

        final List<StoreSizeResponseDTO> finalStoreSizeProfiles = storeSizeProfiles;
        final List<OnlineSizeResponseDTO> finalOnlineSizeProfiles = onlineSizeProfiles;
        styleDTOs.forEach(styleDTO -> strategyStyleDTOList.add(getStyleStrategy(styleDTO, eventType, payloadDTO, finelinePayloadDTO, lvl3Strategy, isSizePackStrategyEnabled, finalStoreSizeProfiles, finalOnlineSizeProfiles)));

        strategyFinelinesDTO.setStyles(strategyStyleDTOList);

        if (isSizePackStrategyEnabled || isInitialLoadAndKafkaSizePackEnabled(eventType) ) {
            StrategyDTO strategyDTO = strategyFinelinesDTO.getStrategy();
            handleFinelineSizeClusters(strategyDTO, finelinePayloadDTO, lvl3Strategy, storeSizeProfiles);

            //If any child Styles have a cluster that the Fineline doesn't, we want to add that to the cluster list
            if (!finelinePayloadDTO.getChannel().equalsIgnoreCase(ONLINE))
                addUnmatchedStyleStoreClustersToFineline(strategyFinelinesDTO, strategyDTO,lvl3Strategy);
        }
        return strategyFinelinesDTO;
    }

    private static CurrentMetricsDTO getCurrentMetricsDTO(CustomerChoiceDTO customerChoiceDTO) {
        return customerChoiceDTO.getMetrics().getCurrent();
    }

    private StrategyDTO getStrategyDTO(HeaderDTO headerDTO, String eventType, FinelinePayloadDTO finelinePayloadDTO, TypeSpecificDTO typeSpecificDTO, String planDesc) {
        StrategyDTO strategyDTO = createDefaultStrategy();
        if (eventType.equalsIgnoreCase(EventType.CREATE.name()) ||
                eventType.equalsIgnoreCase(EventType.UPDATE.name()) ||
                (eventType.equalsIgnoreCase(EventType.INITIAL_LOAD.name())
                        && Boolean.parseBoolean(kafkaProperties.getKafkaConsumerAssortProductFlag())) ||
                eventType.equalsIgnoreCase(EventType.DELETE.name())
        ) {
            strategyDTO.setWeatherClusters(getWeatherClusterStrategy(headerDTO, finelinePayloadDTO, eventType, planDesc));
            if (eventType.equalsIgnoreCase(EventType.CREATE.name()) || (eventType.equalsIgnoreCase(EventType.INITIAL_LOAD.name())
                    && Boolean.parseBoolean(kafkaProperties.getKafkaConsumerAssortProductFlag())) ||
                    kafkaProperties.getKafkaConsumerAcceptedUpdates().stream()
                            .anyMatch(Optional.ofNullable(headerDTO.getChangeScope()).orElse(new ChangeScopeDTO())
                                    .getUpdatedAttributes()::contains)) {
                strategyDTO.setFixture(getFixtureStrategy(typeSpecificDTO));
            }
        }

        return strategyDTO;
    }

    /**
     * At fineline level for stores, we need the sizes from lvl3Strategy to supply to all clusters.  All clusters > 0
     * need sizes and the associated size profile % from Midas.  For online, we only need cluster 0 with lvl3 strategy sizes
     */
    private void handleFinelineSizeClusters(StrategyDTO strategyDTO, FinelinePayloadDTO finelinePayloadDTO, StrategyDTO lvl3Strategy, List<StoreSizeResponseDTO> storeSizeProfiles) {
        final String channel = finelinePayloadDTO.getChannel();

        if (isStoreOrOmni(channel)) {
            final List<StoreSizeResponseDTO> filteredStoreSizeProfiles = storeSizeProfiles.stream()
                  .filter(storeSizeResponseDTO -> storeSizeResponseDTO.getColor_family().equalsIgnoreCase(DEFAULT_COLOR))
                  .collect(Collectors.toList());

            final Set<Integer> clusterIds = getClusterIdsFromSizeProfiles(filteredStoreSizeProfiles);

            //Want to add the 'overall' cluster if it's not present
            clusterIds.add(DEFAULT_CLUSTER_ID);

            List<SizeCluster> clusters = clusterIds.stream().map(clusterId -> {
                SizeCluster cluster = createDefaultSizeCluster(clusterId);
                List<SizeProfileDTO> sizeProfileClusters = copy(getSizeProfileDTOSFromLvl3StrtgyClusters(lvl3Strategy.getStoreSizeClusters()));

                if (clusterId > 0) {
                    List<StoreSizeResponseDTO> filtered = filteredStoreSizeProfiles.stream()
                          .filter(response -> response.getCluster_id().equals(clusterId))
                          .collect(Collectors.toList());
                    mapSizeProfilesToClusterSizes(sizeProfileClusters, filtered);
                }
                cluster.setSizeProfiles(sizeProfileClusters);

                return cluster;
            }).collect(Collectors.toList());

            strategyDTO.getStoreSizeClusters().addAll(clusters);

        }
        if (isOnlineOrOmni(channel)) {
            // Setting all AHS sizes to Cluster 0
            if (strategyDTO.getOnlineSizeClusters().isEmpty()) {
                strategyDTO.getOnlineSizeClusters().add(createDefaultCluster());
            }
            SizeCluster allOnlineCluster = strategyDTO.getOnlineSizeClusters().get(0);
            allOnlineCluster.setSizeProfiles(copy(getSizeProfileDTOSFromLvl3StrtgyClusters(lvl3Strategy.getOnlineSizeClusters())));
        }
    }

    private StrategyStyleDTO getStyleStrategy(StyleDTO styleDTO, String eventType, PayloadDTO payloadDTO, FinelinePayloadDTO finelineDto, StrategyDTO lvl3Strategy, boolean isSizePackStrategyEnabled, List<StoreSizeResponseDTO> storeSizeProfiles, List<OnlineSizeResponseDTO> onlineSizeProfiles) {
        StrategyStyleDTO strategyStyleDTO = new StrategyStyleDTO();
        strategyStyleDTO.setStyleNbr(styleDTO.getStyleNbr());
        strategyStyleDTO.setAltStyleDesc(styleDTO.getAltStyleDesc());
        strategyStyleDTO.setChannel(styleDTO.getChannel());
        strategyStyleDTO.setBaseItemId(styleDTO.getBaseItemId());
        List<StrategyCustomerChoiceDTO> strategyCustomerChoiceDTOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(styleDTO.getCustomerChoices())) {
            styleDTO.getCustomerChoices().forEach(customerChoiceDTO -> {
                  customerChoiceDTO.setFinelineId(finelineDto.getFinelineId());
                  customerChoiceDTO.setBaseItemId(styleDTO.getBaseItemId());
                  strategyCustomerChoiceDTOList.add(getCustomerChoiceStrategy(customerChoiceDTO, eventType, payloadDTO, lvl3Strategy, isSizePackStrategyEnabled, storeSizeProfiles, onlineSizeProfiles));
            });
        }

        strategyStyleDTO.setCustomerChoices(strategyCustomerChoiceDTOList);
        StrategyDTO strategyDTO = createDefaultStrategy();
        strategyStyleDTO.setStrategy(strategyDTO);
        if (isSizePackStrategyEnabled || isInitialLoadAndKafkaSizePackEnabled(eventType)) {
            handleStyleSizeClusters(strategyDTO, strategyStyleDTO, lvl3Strategy, onlineSizeProfiles);

            //If any child CCs have a cluster that the Style doesn't, we want to add that to the cluster list
            if (!styleDTO.getChannel().equalsIgnoreCase(ONLINE))
                addUnmatchedCcStoreClustersToStyle(strategyStyleDTO, strategyDTO, lvl3Strategy);
        }
        return strategyStyleDTO;
    }

    private boolean isInitialLoadAndKafkaSizePackEnabled(String eventType) {
       return eventType.equalsIgnoreCase(EventType.INITIAL_LOAD.name()) && isKafkaConsumerSizePackFlagEnabled();
    }

    /**
     * At style level for online, we need the sizes from lvl3Strategy to supply to cluster 0 with a default color.
     * For stores, we only need cluster 0 with lvl3 strategy sizes
     */
    private void handleStyleSizeClusters(StrategyDTO strategyDTO, StrategyStyleDTO strategyStyleDTO, StrategyDTO lvl3Strategy, List<OnlineSizeResponseDTO> onlineSizeProfiles) {
        final String channel = strategyStyleDTO.getChannel();

        if (isStoreOrOmni(channel)) {
            // Setting all AHS sizes to Cluster 0
            if (strategyDTO.getStoreSizeClusters().isEmpty()) {
                strategyDTO.getStoreSizeClusters().add(createDefaultCluster());
            }
            SizeCluster allStoreCluster = strategyDTO.getStoreSizeClusters().get(0);
            allStoreCluster.setSizeProfiles(copy(getSizeProfileDTOSFromLvl3StrtgyClusters(lvl3Strategy.getStoreSizeClusters())));
        }

        if (isOnlineOrOmni(channel)) {
            List<OnlineSizeResponseDTO> onlineSizeProfilesByColor = new ArrayList<>();

            if (strategyStyleDTO.getBaseItemId() != null) {
                onlineSizeProfilesByColor = onlineSizeProfiles.stream().filter(onlineSizeResponseDTO -> onlineSizeResponseDTO.getColor_family().equalsIgnoreCase(DEFAULT_COLOR)
                      && strategyStyleDTO.getBaseItemId().equals(String.valueOf(onlineSizeResponseDTO.getBase_itm_id()))).collect(Collectors.toList());
            }

            List<SizeProfileDTO> sizeProfileClusters = copy(getSizeProfileDTOSFromLvl3StrtgyClusters(lvl3Strategy.getOnlineSizeClusters()));
            mapSizeProfilesToClusterSizes(sizeProfileClusters, onlineSizeProfilesByColor);

            if (strategyDTO.getOnlineSizeClusters().isEmpty())
                strategyDTO.getOnlineSizeClusters().add(createDefaultCluster());

            SizeCluster allOnlineCluster = strategyDTO.getOnlineSizeClusters().get(0);
            allOnlineCluster.setSizeProfiles(sizeProfileClusters);
        }
    }

    private StrategyCustomerChoiceDTO getCustomerChoiceStrategy(CustomerChoiceDTO customerChoiceDTO, String eventType, PayloadDTO payloadDTO, StrategyDTO lvl3Strategy, boolean isSizePackStrategyEnabled, List<StoreSizeResponseDTO> storeSizeProfiles, List<OnlineSizeResponseDTO> onlineSizeProfiles) {
        StrategyCustomerChoiceDTO strategyCustomerChoiceDTO = new StrategyCustomerChoiceDTO();
        strategyCustomerChoiceDTO.setCcId(customerChoiceDTO.getCcId());
        strategyCustomerChoiceDTO.setAltCcDesc(customerChoiceDTO.getAltCcDesc());
        strategyCustomerChoiceDTO.setChannel(customerChoiceDTO.getChannel());

        TypeSpecificDTO typeSpecificDTO = Optional.ofNullable(customerChoiceDTO.getMetrics())
                .map(MetricsDTO::getCurrent)
                .map(CurrentMetricsDTO::getStore)
                .map(StoreMetricsDTO::getProductAttributes)
                .map(ProductAttributesDTO::getTypeSpecific)
                .orElse(new TypeSpecificDTO());

        TypeSpecificDTO typeSpecificDTOForOnline = Optional.ofNullable(customerChoiceDTO.getMetrics())
                .map(MetricsDTO::getCurrent)
                .map(CurrentMetricsDTO::getOnline)
                .map(OnlineMetricsDTO::getProductAttributes)
                .map(ProductAttributesDTO::getTypeSpecific)
                .orElse(new TypeSpecificDTO());

        FinancialAttributesDTO financialAttributesDTO = Optional.ofNullable(customerChoiceDTO.getMetrics())
                .map(MetricsDTO::getCurrent)
                .map(CurrentMetricsDTO::getStore)
                .map(StoreMetricsDTO::getFinancialAttributes)
                .orElse(new FinancialAttributesDTO());

        if (!CollectionUtils.isEmpty(typeSpecificDTO.getColors())) {
            strategyCustomerChoiceDTO.setColorName(typeSpecificDTO.getColors().get(0).getColorName());
            strategyCustomerChoiceDTO.setColorFamily(typeSpecificDTO.getColors().get(0).getColorFamily());
        }
        if (!CollectionUtils.isEmpty(typeSpecificDTOForOnline.getColors())) {
            strategyCustomerChoiceDTO.setColorName(typeSpecificDTOForOnline.getColors().get(0).getColorName());
            strategyCustomerChoiceDTO.setColorFamily(typeSpecificDTO.getColors().get(0).getColorFamily());
        }
        String channel = customerChoiceDTO.getChannel();
        TypeSpecificDTO typeSpecificDTOcc =new TypeSpecificDTO();
        if (isStoreOrOmni(channel)){
            typeSpecificDTOcc = Optional.ofNullable(customerChoiceDTO.getMetrics())
                    .map(MetricsDTO::getCurrent)
                    .map(CurrentMetricsDTO::getStore)
                    .map(StoreMetricsDTO::getProductAttributes)
                    .map(ProductAttributesDTO::getTypeSpecific)
                    .orElse(new TypeSpecificDTO());
        }
        final Boolean startDate = typeSpecificDTOcc.getIsTransactableStartWkEditedInAP();
        final Boolean endDate = typeSpecificDTOcc.getIsTransactableEndWkEditedInAP();
        StrategyDTO ccStrategyDTO = createDefaultStrategy();
        if (eventType.equalsIgnoreCase(EventType.CREATE.name()) ||
                eventType.equalsIgnoreCase(EventType.UPDATE.name()) ||
                (eventType.equalsIgnoreCase(EventType.INITIAL_LOAD.name())
                        && Boolean.parseBoolean(kafkaProperties.getKafkaConsumerAssortProductFlag())) ||
                eventType.equalsIgnoreCase(EventType.DELETE.name())
        ) {
            List<WeatherClusterDTO> ccWeatherClusterDTOList = new ArrayList<>();
            WeatherClusterDTO ccWeatherClusterDTO = new WeatherClusterDTO();
            WeatherClusterTypeDTO weatherClusterTypeDTO = new WeatherClusterTypeDTO();
            weatherClusterTypeDTO.setAnalyticsClusterId(0);
            weatherClusterTypeDTO.setAnalyticsClusterDesc("all");
            ccWeatherClusterDTO.setType(weatherClusterTypeDTO);
            ccWeatherClusterDTO.setInStoreDate(getDate(financialAttributesDTO.getTransactableStartWk(), financialAttributesDTO.getTransactableStartWkNbr(), null));
            ccWeatherClusterDTO.setMarkDownDate(getDate(financialAttributesDTO.getTransactableEndWk(), financialAttributesDTO.getTransactableEndWkNbr(), null));
            ccWeatherClusterDTO.setMarkDownDisabledInLP(endDate);
            ccWeatherClusterDTO.setInStoreDisabledInLP(startDate);
            ccWeatherClusterDTOList.add(ccWeatherClusterDTO);
            ccStrategyDTO.setWeatherClusters(ccWeatherClusterDTOList);
        }

        //Sizing logic
        if (isSizePackStrategyEnabled || isInitialLoadAndKafkaSizePackEnabled(eventType)) {
            handleCustomerChoiceSizeClusters(ccStrategyDTO, customerChoiceDTO, payloadDTO, lvl3Strategy, storeSizeProfiles, onlineSizeProfiles);
        }

        strategyCustomerChoiceDTO.setStrategy(ccStrategyDTO);


        return strategyCustomerChoiceDTO;
    }

    private List<SizeProfileDTO> getSizeProfileDTOSFromLvl3StrtgyClusters(List<SizeCluster> sizeClusters) {
        List<SizeProfileDTO> sizeProfileDTOS = Collections.emptyList();
        if(Objects.nonNull(sizeClusters) && !sizeClusters.isEmpty()){
            sizeProfileDTOS = sizeClusters.get(0).getSizeProfiles();
        }
        return sizeProfileDTOS;
    }
    private void handleCustomerChoiceSizeClusters(StrategyDTO strategyDTO, CustomerChoiceDTO customerChoiceDTO, PayloadDTO payloadDTO, StrategyDTO lvl3Strategy, List<StoreSizeResponseDTO> storeSizeProfiles, List<OnlineSizeResponseDTO> onlineSizeProfiles) {
        final String channel = customerChoiceDTO.getChannel();
        if (isStoreOrOmni(channel)) {
            String colorFamily = getColorFamilyOrDefault(getCurrentMetricsDTO(customerChoiceDTO).getStore().getProductAttributes());

            final List<StoreSizeResponseDTO> storeSizeProfilesByColor = storeSizeProfiles
                    .stream()
                    .filter(storeSizeResponseDTO -> storeSizeResponseDTO.getColor_family().equalsIgnoreCase(colorFamily))
                    .collect(Collectors.toList());

            if (storeSizeProfilesByColor.isEmpty()) {
                storeSizeProfilesByColor.addAll(storeSizeProfiles.stream()
                      .filter(storeSizeResponseDTO -> storeSizeResponseDTO.getColor_family().equalsIgnoreCase(DEFAULT_COLOR))
                      .collect(Collectors.toList()));
            }

            final Set<Integer> clusterIds = getClusterIdsFromSizeProfiles(storeSizeProfilesByColor);

            if(clusterIds.isEmpty()){
                //Add a default cluster 1 for Store/Omni (store clusters only) if no recommendations received from Midas
                clusterIds.add(DEFAULT_CLUSTER_ID_1);
            }

            //Want to add the 'overall' cluster if it's not present
            clusterIds.add(DEFAULT_CLUSTER_ID);


            List<SizeCluster> clusters = clusterIds.stream().map(clusterId -> {
                SizeCluster cluster = createDefaultSizeCluster(clusterId);
                List<SizeProfileDTO> sizeProfileClusters = copy(getSizeProfileDTOSFromLvl3StrtgyClusters(lvl3Strategy.getStoreSizeClusters()));

                if (clusterId > 0) {
                    List<StoreSizeResponseDTO> filtered = storeSizeProfilesByColor.stream()
                          .filter(response -> response.getCluster_id().equals(clusterId))
                          .collect(Collectors.toList());
                    mapSizeProfilesToClusterSizes(sizeProfileClusters, filtered);
                }
                cluster.setSizeProfiles(sizeProfileClusters);

                return cluster;
            }).collect(Collectors.toList());

            strategyDTO.getStoreSizeClusters().addAll(clusters);
        }

        if (isOnlineOrOmni(channel)) {
            String colorFamily = getColorFamilyOrDefault(getCurrentMetricsDTO(customerChoiceDTO).getOnline().getProductAttributes());

            List<OnlineSizeResponseDTO> onlineSizeProfilesByColor = new ArrayList<>();


            onlineSizeProfilesByColor = getOnlineSizeProfiles(customerChoiceDTO, onlineSizeProfiles, colorFamily, onlineSizeProfilesByColor);


            List<SizeProfileDTO> sizeProfileClusters = copy(getSizeProfileDTOSFromLvl3StrtgyClusters(lvl3Strategy.getOnlineSizeClusters()));
            mapSizeProfilesToClusterSizes(sizeProfileClusters, onlineSizeProfilesByColor);

            if (strategyDTO.getOnlineSizeClusters().isEmpty()) {
                strategyDTO.getOnlineSizeClusters().add(createDefaultCluster());
            }

            SizeCluster allOnlineCluster = strategyDTO.getOnlineSizeClusters().get(0);
            allOnlineCluster.setSizeProfiles(sizeProfileClusters);
        }
    }

    private static List<OnlineSizeResponseDTO> getOnlineSizeProfiles(CustomerChoiceDTO customerChoiceDTO, List<OnlineSizeResponseDTO> onlineSizeProfiles, String colorFamily, List<OnlineSizeResponseDTO> onlineSizeProfilesByColor) {
        if (customerChoiceDTO.getBaseItemId() != null) {
            onlineSizeProfilesByColor = onlineSizeProfiles
                  .stream()
                  .filter(onlineSizeResponseDTO -> onlineSizeResponseDTO.getColor_family().equalsIgnoreCase(colorFamily)
                        && customerChoiceDTO.getBaseItemId().equals(String.valueOf(onlineSizeResponseDTO.getBase_itm_id())))
                  .collect(Collectors.toList());

            if (onlineSizeProfilesByColor.isEmpty()) {
                onlineSizeProfilesByColor.addAll(onlineSizeProfiles.stream().filter(onlineSizeResponseDTO -> onlineSizeResponseDTO.getColor_family().equalsIgnoreCase(DEFAULT_COLOR)
                            && customerChoiceDTO.getBaseItemId().equals(String.valueOf(onlineSizeResponseDTO.getBase_itm_id())))
                      .collect(Collectors.toList()));
            }
        }
        return onlineSizeProfilesByColor;
    }

    private List<WeatherClusterDTO> getWeatherClusterStrategy(HeaderDTO headerDTO, FinelinePayloadDTO finelinePayloadDTO, String eventType, String planDesc) {
        List<WeatherClusterDTO> weatherClusterDTOList = new ArrayList<>();

        FinancialAttributesDTO financialAttributesDTO = Optional.ofNullable(finelinePayloadDTO.getMetrics())
                .map(MetricsDTO::getCurrent)
                .map(CurrentMetricsDTO::getStore)
                .map(StoreMetricsDTO::getFinancialAttributes)
                .orElse(new FinancialAttributesDTO());
        String channel = finelinePayloadDTO.getChannel();
        TypeSpecificDTO typeSpecificDTO = new TypeSpecificDTO();
        if (isStoreOrOmni(channel)) {
            typeSpecificDTO = Optional.ofNullable(finelinePayloadDTO.getMetrics())
                    .map(MetricsDTO::getCurrent)
                    .map(CurrentMetricsDTO::getStore)
                    .map(StoreMetricsDTO::getProductAttributes)
                    .map(ProductAttributesDTO::getTypeSpecific)
                    .orElse(new TypeSpecificDTO());
        }
        final Boolean startDate = typeSpecificDTO.getIsTransactableStartWkEditedInAP();
        final Boolean endDate = typeSpecificDTO.getIsTransactableEndWkEditedInAP();

        //TODO: Handle scenarios to not to update instore and markdown dates user edited in AP with CLP dates
        //Call Midas API for create event and fineline channel is store or omni
        //Call Midas API for update event and fineline channel is changed from online to store or omni
        if (isCreateOrUpdate(headerDTO, finelinePayloadDTO, eventType)) {
            FinelineRankMetricsDTO finelineRankMetricsDTO = getFinelineRankMetrics(headerDTO, planDesc);
            ResultDTO resultDTO = Optional.ofNullable(finelineRankMetricsDTO.getPayload())
                    .map(FinelineRankMetricsPayloadDTO::getResult)
                    .orElse(new ResultDTO());
            if (CollectionUtils.isEmpty(resultDTO.getResponse())) {
                getDefaultWeatherClusterStrategy(financialAttributesDTO, weatherClusterDTOList, startDate, endDate);
            } else {
                resultDTO.getResponse().forEach(responseDTO -> {
                    WeatherClusterDTO weatherClusterDTO = new WeatherClusterDTO();
                    weatherClusterDTO.setForecastedSales(responseDTO.getForecastedDemandSales());
                    WeatherClusterTypeDTO weatherClusterTypeDTO = new WeatherClusterTypeDTO();
                    weatherClusterTypeDTO.setAnalyticsClusterId(responseDTO.getAnalyticsClusterId());
                    if (responseDTO.getAnalyticsClusterId() == 0) {
                        weatherClusterTypeDTO.setAnalyticsClusterDesc("all");
                    } else {
                        weatherClusterTypeDTO.setAnalyticsClusterDesc("cluster ".concat(responseDTO.getAnalyticsClusterId().toString()));
                    }
                    weatherClusterDTO.setType(weatherClusterTypeDTO);
                    weatherClusterDTO.setForecastedUnits(responseDTO.getForecastedDemandUnits());
                    weatherClusterDTO.setLySales(responseDTO.getSalesAmtLastYr());
                    weatherClusterDTO.setLyUnits(responseDTO.getSalesUnitsLastYr());
                    weatherClusterDTO.setOnHandQty(responseDTO.getOnHandQty());
                    weatherClusterDTO.setSalesToStockRatio(responseDTO.getSalesToStockRatio());
                    weatherClusterDTO.setAlgoClusterRanking(responseDTO.getRank());
                    weatherClusterDTO.setInStoreDate(getDate(financialAttributesDTO.getTransactableStartWk(), financialAttributesDTO.getTransactableStartWkNbr(), null));
                    weatherClusterDTO.setMarkDownDate(getDate(financialAttributesDTO.getTransactableEndWk(), financialAttributesDTO.getTransactableEndWkNbr(), null));
                    weatherClusterDTO.setInStoreDisabledInLP(startDate);
                    weatherClusterDTO.setMarkDownDisabledInLP(endDate);
                    weatherClusterDTOList.add(weatherClusterDTO);
                });

            }
        } else {
            getDefaultWeatherClusterStrategy(financialAttributesDTO, weatherClusterDTOList, startDate, endDate);
        }
        return weatherClusterDTOList;
    }

    private static boolean isCreateOrUpdate(HeaderDTO headerDTO, FinelinePayloadDTO finelinePayloadDTO, String eventType) {
        return ((eventType.equalsIgnoreCase(EventType.CREATE.name()) || eventType.equalsIgnoreCase(EventType.INITIAL_LOAD.name()))
                && !(finelinePayloadDTO.getChannel().equalsIgnoreCase("online")))
                || ((Optional.ofNullable(headerDTO.getChangeScope()).orElse(new ChangeScopeDTO()).getUpdatedAttributes().stream().anyMatch("channel"::equalsIgnoreCase))
                && !(finelinePayloadDTO.getChannel().equalsIgnoreCase("online")));
    }

    private void getDefaultWeatherClusterStrategy(FinancialAttributesDTO financialAttributesDTO, List<WeatherClusterDTO> weatherClusterDTOList, Boolean inStoreDisabledInLP, Boolean markDownDisabledInLP) {
        WeatherClusterDTO weatherClusterDTO = new WeatherClusterDTO();
        WeatherClusterTypeDTO weatherClusterTypeDTO = new WeatherClusterTypeDTO();
        weatherClusterTypeDTO.setAnalyticsClusterId(0);
        weatherClusterTypeDTO.setAnalyticsClusterDesc("all");
        weatherClusterDTO.setType(weatherClusterTypeDTO);
        weatherClusterDTO.setInStoreDate(getDate(financialAttributesDTO.getTransactableStartWk(), financialAttributesDTO.getTransactableStartWkNbr(), null));
        weatherClusterDTO.setMarkDownDate(getDate(financialAttributesDTO.getTransactableEndWk(), financialAttributesDTO.getTransactableEndWkNbr(), null));
        weatherClusterDTO.setInStoreDisabledInLP(inStoreDisabledInLP);
        weatherClusterDTO.setMarkDownDisabledInLP(markDownDisabledInLP);
        weatherClusterDTOList.add(weatherClusterDTO);
    }

    private DateDTO getDate(String fiscalWeekDesc, Long wmYearWeek, Long dwWeekId) {
        return new DateDTO(fiscalWeekDesc, wmYearWeek, dwWeekId);
    }

    private List<FixtureDTO> getFixtureStrategy(TypeSpecificDTO typeSpecificDTO) {
        List<FixtureDTO> fixtureDTOList = new ArrayList<>();
        if (typeSpecificDTO.getPreferredFixtureType() != null) {
            setOrderPref(typeSpecificDTO.getPreferredFixtureType(), fixtureDTOList, OrderPrefType.ORDER_PREF_1.getValue());
        }
        if (typeSpecificDTO.getPreferredFixtureType2() != null) {
            setOrderPref(typeSpecificDTO.getPreferredFixtureType2(), fixtureDTOList, OrderPrefType.ORDER_PREF_2.getValue());
        }
        if (typeSpecificDTO.getPreferredFixtureType3() != null) {
            setOrderPref(typeSpecificDTO.getPreferredFixtureType3(), fixtureDTOList, OrderPrefType.ORDER_PREF_3.getValue());
        }
        if (typeSpecificDTO.getPreferredFixtureType4() != null) {
            setOrderPref(typeSpecificDTO.getPreferredFixtureType4(), fixtureDTOList, OrderPrefType.ORDER_PREF_4.getValue());
        }
        return fixtureDTOList;
    }

    private void setOrderPref(String preferredType, List<FixtureDTO> fixtureDTOList, Integer orderPrefValue) {
        FixtureDTO orderPref = new FixtureDTO();
        orderPref.setOrderPref(orderPrefValue);
        orderPref.setType(preferredType);
        fixtureDTOList.add(orderPref);
    }

    /*
    * Size Logic below here will eventually be moved to its own processing class
    *
    * */
    private boolean isSizeStrategyEnabled() {
        return Boolean.parseBoolean(ahsApiProperties.getSizePackReleaseFlag());
    }


    private SizeCluster createDefaultCluster() {
        ClusterType type = new ClusterType(DEFAULT_CLUSTER_ID, DEFAULT_CLUSTER_DESC);
        return new SizeCluster(type, new ArrayList<>());
    }

    private Set<Integer> getClusterIdsFromSizeProfiles(List<? extends SizeProfileResponse> sizeProfiles) {
        return sizeProfiles.stream()
              .map(SizeProfileResponse::getCluster_id)
              .collect(Collectors.toSet());
    }

    private void mapSizeProfilesToClusterSizes(List<SizeProfileDTO> sizeProfiles, List<? extends SizeProfileResponse> storeSizeProfiles) {
        Map<String, Double> sizeDescToProfilePct = storeSizeProfiles
              .stream()
              .collect(Collectors.toMap(SizeProfileResponse::getSize_desc, SizeProfileResponse::getSize_profile));

        sizeProfiles.forEach(size -> {
            //We're setting both to whatever size profile %
            size.setSizeProfilePrcnt(sizeDescToProfilePct.get(size.getSizeDesc()));
            size.setAdjustedSizeProfile(sizeDescToProfilePct.get(size.getSizeDesc()));
        });
    }

    private List<SizeProfileDTO> copy(List<SizeProfileDTO> sizeProfiles) {
        try {
            final String copied = objectMapper.writeValueAsString(sizeProfiles);
            return Arrays.asList(objectMapper.readValue(copied, SizeProfileDTO[].class));
        } catch (JsonProcessingException e) {
            log.error("Copy of size profiles failed: {}", e.toString());
        }
        return new ArrayList<>();
    }

    private FinelineRankMetricsDTO getFinelineRankMetrics(HeaderDTO header, String planDesc) {
        FinelineRankMetricsDTO finelineRankMetricsDTO = new FinelineRankMetricsDTO();
        try {
            finelineRankMetricsDTO = midasServiceCall.invokeMidasApi(header, planDesc);
        } catch (Exception e) {
            log.error("Error parsing response for Ranking from Midas: {}", e.getMessage());
        }
        return finelineRankMetricsDTO;
    }

    public List<StoreSizeResponseDTO> getStoreSizeProfilesV2(String planDesc, Long deptNbr, Long catgNbr, Long subCatgNbr, Long finelineNbr, Set<String> colorFamilies) {
        List<StoreSizeResponseDTO> storeSizeProfiles = new ArrayList<>();
        planDesc = sizePackProperties.isDefaultPlanDesc() ? sizePackProperties.getDefaultPlanDesc() : planDesc;
        String parsedSeason = getSeasonOrDefault(planDesc);
        String year = getYearFromPlanDesc(planDesc);
        colorFamilies = formatColorFamilies(colorFamilies);

        try {
            StoreSizeProfileRequestDTO request = new StoreSizeProfileRequestDTO(finelineNbr, parsedSeason, deptNbr, catgNbr, subCatgNbr, year, colorFamilies);
            storeSizeProfiles = midasServiceCall.getStoreSizeProfilesV2(request);
        } catch (Exception e) {
            log.error("Error parsing response for Store Size Profile from Midas: {}", e.getMessage());
        }
        return storeSizeProfiles;
    }

    public List<OnlineSizeResponseDTO> getOnlineSizeProfilesV2(String planDesc, Long deptNbr, Set<String> baseItemIds, Set<String> colorFamilies) {
        List<OnlineSizeResponseDTO> onlineSizeProfiles = new ArrayList<>();
        planDesc = sizePackProperties.isDefaultPlanDesc() ? sizePackProperties.getDefaultPlanDesc() : planDesc;
        final String parsedSeason = getSeasonOrDefault(planDesc);
        String year = getYearFromPlanDesc(planDesc);
        colorFamilies = formatColorFamilies(colorFamilies);

        try {
            OnlineSizeProfileRequestDTO request = new OnlineSizeProfileRequestDTO(baseItemIds, parsedSeason, deptNbr, year, colorFamilies);
            onlineSizeProfiles = midasServiceCall.getOnlineSizeProfilesV2(request);
        } catch (Exception e) {
            log.error("Error parsing response for Online Size Profile from Midas: {}", e.getMessage());
        }
        return onlineSizeProfiles;
    }

    private Set<String> formatColorFamilies(Set<String> colorFamilies) {
        return colorFamilies.stream().map(String::toUpperCase).collect(Collectors.toSet());
    }

    private String getYearFromPlanDesc(String planDesc) {
        if(planDesc == null || planDesc.length() < 4) {
            return null;
        }
        String year = planDesc.substring(planDesc.length() - 4);
        if(year.matches("\\d+")) {
            return year;
        } else {
            return null;
        }
    }

    private String getSeasonOrDefault(String season) {
        String parsedSeason;
        try {
            //Typical format: "S3 - FYE2024"
            parsedSeason = season.substring(0, 2);
            log.debug("parsed season: {}", parsedSeason);
        } catch (NullPointerException|IndexOutOfBoundsException e) {
            parsedSeason = DEFAULT_SEASON;
        }
        return parsedSeason;
    }

    public void addUnmatchedStyleStoreClustersToFineline(StrategyFinelinesDTO strategyFinelineDto, StrategyDTO strategyDTO,StrategyDTO lvl3Strategy) {
        Set<Integer> finelineClusterIds = getStoreClusterIdsFromSizeClusters(strategyDTO.getStoreSizeClusters().stream());

        Set<Integer> styleSizeClusterIds = getStyleSizeClusterIds(strategyFinelineDto.getStyles());
        List<SizeProfileDTO> sizeProfileClusters = copy(getSizeProfileDTOSFromLvl3StrtgyClusters(lvl3Strategy.getStoreSizeClusters()));
        styleSizeClusterIds.stream()
              .filter(ccClusterId -> !finelineClusterIds.contains(ccClusterId))
              .forEach(ccClusterId -> {
                  SizeCluster sizeCluster = new SizeCluster(new ClusterType(ccClusterId, null), sizeProfileClusters);
                  strategyDTO.getStoreSizeClusters().add(sizeCluster);
              });
    }

    public void addUnmatchedCcStoreClustersToStyle(StrategyStyleDTO strategyStyleDTO, StrategyDTO strategyDTO, StrategyDTO lvl3Strategy) {
        Set<Integer> styleClusterIds = getStoreClusterIdsFromSizeClusters(strategyDTO.getStoreSizeClusters().stream());

        Set<Integer> ccClusterIds = getCustomerChoiceSizeClusterIds(strategyStyleDTO.getCustomerChoices());
        List<SizeProfileDTO> sizeProfileClusters = copy(getSizeProfileDTOSFromLvl3StrtgyClusters(lvl3Strategy.getStoreSizeClusters()));
        ccClusterIds.stream()
              .filter(ccClusterId -> !styleClusterIds.contains(ccClusterId))
              .forEach(ccClusterId -> {
                  SizeCluster sizeCluster = new SizeCluster(new ClusterType(ccClusterId, null), sizeProfileClusters);
                  strategyDTO.getStoreSizeClusters().add(sizeCluster);
              });
    }

    private Set<Integer> getStyleSizeClusterIds(List<StrategyStyleDTO> styles) {
        return getStoreClusterIdsFromStrategies(styles.stream()
              .map(StrategyStyleDTO::getStrategy));
    }

    private Set<Integer> getCustomerChoiceSizeClusterIds(List<StrategyCustomerChoiceDTO> ccs) {
        return getStoreClusterIdsFromStrategies(ccs.stream()
              .map(StrategyCustomerChoiceDTO::getStrategy));
    }

    private Set<Integer> getStoreClusterIdsFromStrategies(Stream<StrategyDTO> strategies) {
        return getStoreClusterIdsFromSizeClusters(strategies.map(StrategyDTO::getStoreSizeClusters)
              .flatMap(Collection::stream));
    }

    private Set<Integer> getStoreClusterIdsFromSizeClusters(Stream<SizeCluster> sizeClusters) {
        return sizeClusters.map(SizeCluster::getType)
              .map(ClusterType::getAnalyticsClusterId)
              .collect(Collectors.toSet());
    }


    private String getColorFamilyOrDefault(ProductAttributesDTO productAttribute) {
        String colorFamily;
        try {
            colorFamily = productAttribute.getTypeSpecific().getColors().get(0).getColorFamily();
            if (colorFamily == null)
                colorFamily = DEFAULT_COLOR;
        } catch (NullPointerException|IndexOutOfBoundsException e) {
            colorFamily = DEFAULT_COLOR;
        }
        return colorFamily;
    }

    /**
     * Attaches all applicable sizes to L3 for Online and Store
     * @param categoryId
     * @param departmentId
     * @return SizeProfileClusteStrategyDTO
     */
    private StrategyDTO getSizeProfileStrategyDTO(Long categoryId, Long departmentId) {
        StrategyDTO strategyDTO = createDefaultStrategy();
        strategyDTO.setStoreSizeClusters(List.of(createDefaultCluster()));
        strategyDTO.setOnlineSizeClusters(List.of(createDefaultCluster()));
        List<SizeProfileDTO> sizeProfiles = getLvl3EligibleSizes(categoryId.intValue(), departmentId);
        strategyDTO.getStoreSizeClusters().get(0).setSizeProfiles(sizeProfiles);
        strategyDTO.getOnlineSizeClusters().get(0).setSizeProfiles(sizeProfiles);
        return strategyDTO;
    }

    /**
     * @return StrategyDTO with empty store and online size clusters
     */
    private StrategyDTO createDefaultStrategy() {
        StrategyDTO strategyDTO = new StrategyDTO();
        strategyDTO.setStoreSizeClusters(new ArrayList<>());
        strategyDTO.setOnlineSizeClusters(new ArrayList<>());
        return strategyDTO;
    }

    private SizeCluster createDefaultSizeCluster(Integer clusterId) {
        return new SizeCluster(new ClusterType(clusterId, null), new ArrayList<>());
    }

    /***
     * This function is used to get Size details from AHS call and send back the list of Size Profile DTO
     * @param categoryId
     * @param departmentId
     * @return List<SizeProfileClusterDTO>
     */
    public List<SizeProfileDTO> getLvl3EligibleSizes(Integer categoryId, Long departmentId) {
        List<SizeProfileDTO> sizeProfileDTOList = new ArrayList<>();
        SizeDetailResponseDTO[] result = strategyAHSCallService.getSizeDetailsFromAHS(categoryId, departmentId);
        if (result != null && result.length > 0) {
            for (SizeDetailResponseDTO sizeDetailResponse : result) {
                if (sizeDetailResponse.getId() != null) {
                    SizeProfileDTO sizeProfile = new SizeProfileDTO();
                    sizeProfile.setAhsSizeId(sizeDetailResponse.getId());
                    sizeProfile.setIsEligible(DEFAULT_IS_ELIGIBLE);
                    sizeProfile.setSizeDesc(sizeDetailResponse.getValue());
                    sizeProfileDTOList.add(sizeProfile);
                }
            }
        }
        return sizeProfileDTOList;
    }

    private boolean isOnlineOrOmni(String channel) {
        return channel != null && (channel.equalsIgnoreCase(ONLINE) || channel.equalsIgnoreCase(OMNI));
    }

    private boolean isStoreOrOmni(String channel) {
        return channel != null && (channel.equalsIgnoreCase(STORE) || channel.equalsIgnoreCase(OMNI));
    }

    private boolean isSizePackStrategyEnabled(String eventType, HeaderDTO headerDTO) {
        return (isSizeStrategyEnabled() && !eventType.equalsIgnoreCase(EventType.DELETE.name())) &&
                (eventType.equalsIgnoreCase(EventType.CREATE.name()) ||
                        (eventType.equalsIgnoreCase(EventType.INITIAL_LOAD.name())) ||
                             sizePackProperties.getAcceptedSizePackUpdates().stream()
                                  .anyMatch(Optional.ofNullable(headerDTO.getChangeScope()).orElse(new ChangeScopeDTO())
                                       .getUpdatedAttributes()::contains));
    }

}
