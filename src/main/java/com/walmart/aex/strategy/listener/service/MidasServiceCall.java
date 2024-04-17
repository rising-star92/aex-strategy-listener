package com.walmart.aex.strategy.listener.service;

import com.walmart.aex.strategy.listener.dto.HeaderDTO;
import com.walmart.aex.strategy.listener.dto.OnlineSizeProfileRequestDTO;
import com.walmart.aex.strategy.listener.dto.StoreSizeProfileRequestDTO;
import com.walmart.aex.strategy.listener.dto.midas.FinelineRankMetricsDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.OnlineSizeProfileDataDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.OnlineSizeProfilePayloadDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.OnlineSizeProfileResultDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.OnlineSizeResponseDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.StoreSizeProfileDataDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.StoreSizeProfilePayloadDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.StoreSizeProfileResultDTO;
import com.walmart.aex.strategy.listener.dto.midas.response.StoreSizeResponseDTO;
import com.walmart.aex.strategy.listener.exception.ClpApListenerException;
import com.walmart.aex.strategy.listener.properties.MidasApiProperties;
import com.walmart.aex.strategy.listener.properties.CredProperties;
import io.strati.ccm.utils.client.annotation.ManagedConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MidasServiceCall {

    @ManagedConfiguration
    private MidasApiProperties midasProperties;

	private RestTemplate restTemplate;

	private CredProperties credProperties;

	public MidasServiceCall(@Qualifier("restTemplateWithShortTimeout") RestTemplate restTemplate, CredProperties credProperties) {
		this.restTemplate = restTemplate;
		this.credProperties = credProperties;
	}

	@Retryable(maxAttempts = 5, backoff = @Backoff(delay = 10000))
    public FinelineRankMetricsDTO invokeMidasApi(HeaderDTO header, String planDesc) {
        try {
            Integer lvl1Nbr = header.getChangeScope().getStrongKeys().getLvl1Nbr();
            Integer lvl2Nbr = header.getChangeScope().getStrongKeys().getLvl2Nbr();
            Long finelineNbr = header.getChangeScope().getStrongKeys().getFineline().getFinelineId();
            String season = "'"+planDesc.substring(0,2)+"'";
            Integer fiscalYear = Integer.valueOf(planDesc.substring(planDesc.length()- 4));

            String query = String.format(midasProperties.getAPRankingMetricsQuery(), lvl1Nbr, lvl2Nbr, finelineNbr, season, fiscalYear);
            String url = midasProperties.getMidasApiBaseURL();
            log.info("Invoking Midas API for Create event with URL: {} | query: {}", url, query);

            ResponseEntity<FinelineRankMetricsDTO> result =
                    restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(query, getHeadersForMidas()), FinelineRankMetricsDTO.class);
            log.info("Successfully invoked Midas Api with result: {}", result);
            return result.getBody();
        } catch (Exception e) {
            log.error("Exception in Getting Midas Data for the given Input: {}", e.getMessage());
            throw new ClpApListenerException("Exception calling midas for analytics metrics: " + header.toString(), e);
        }
    }

    private HttpHeaders getHeadersForMidas() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("WM_SVC.NAME", "aex-MIDAS-Service-Call");
        headers.set("consumer", midasProperties.getMidasHeaderConsumer());
        headers.set("signature_key_version", midasProperties.getMidasHeaderSignatureKeyVersion());
        headers.set("signature_ts", midasProperties.getMidasHeaderSignatureTS());
        headers.set("signature_auth_flag", midasProperties.getMidasHeaderSignatureAuthFlag());
        headers.set("request_ts", String.valueOf(Instant.now().getEpochSecond()));
        headers.set("tenant", midasProperties.getMidasHeaderTenant());
        headers.set("Authorization", credProperties.fetchMidasApiAuthorization());
        return headers;
    }

    @Recover
    public FinelineRankMetricsDTO recover(Exception e, HeaderDTO header) {
        throw new ClpApListenerException("Timeout calling midas for analytics metrics: " + header.toString(), e);
    }

	@Retryable(backoff = @Backoff(delay = 10000))
    public List<OnlineSizeResponseDTO> getOnlineSizeProfiles(OnlineSizeProfileRequestDTO onlineSizeProfileInput)
	{
		List<OnlineSizeResponseDTO> onlineSizeProfiles = new ArrayList<>();
		String colorFamily;
		String season;
		String baseItemId = null;
		String colorFamilyFinalStr = null;
		String seasonFinalStr = null;
		Long deptNbr = null;
		String year = null;
		try {
			baseItemId = onlineSizeProfileInput.getBaseItemId();
			colorFamily = onlineSizeProfileInput.getColorFamily();
			colorFamilyFinalStr = getProperStringFormatForQuery(colorFamily);
			season = onlineSizeProfileInput.getSeason();
			seasonFinalStr = getProperStringFormatForQuery(season);
			deptNbr = onlineSizeProfileInput.getDeptNbr();
			year = onlineSizeProfileInput.getYear();

			String query = String.format(midasProperties.getOnlineSizeProfilesQuery(), seasonFinalStr, deptNbr, baseItemId, colorFamilyFinalStr, year);
			log.debug("Online size profile midas request: {}", query);
			String url = midasProperties.getMidasApiBaseURL();
			log.info("Invoking Midas API for Create event with URL: {} | query: {}", url, query);

			ResponseEntity<OnlineSizeProfileDataDTO> result =
					restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(query, getHeadersForMidas()), OnlineSizeProfileDataDTO.class);
			log.info("Successfully invoked Midas Api with result: {}", result);

			onlineSizeProfiles = Optional.ofNullable(result.getBody())
					.map(OnlineSizeProfileDataDTO::getPayload)
					.map(OnlineSizeProfilePayloadDTO::getResult)
					.map(OnlineSizeProfileResultDTO::getResponse)
					.orElse(new ArrayList<>());
			return onlineSizeProfiles;
		} catch (IllegalFormatConversionException ifce) {
			log.error("Online size profile input args invalid.  Skipping lookup.  season: {}, deptNbr: {}, baseItemId: {}, colorFamily: {}",
					seasonFinalStr, deptNbr, baseItemId, colorFamilyFinalStr);
			throw new ClpApListenerException("Online size profile input args invalid");

		} catch (Exception e) {
			log.error("Exception in Getting Online profile data from Midas for the given Input: {}", e.getMessage());
			throw new ClpApListenerException("Exception in Getting Online Size profile");
		}

	}

	@Retryable(maxAttempts = 5, backoff = @Backoff(delay = 3000))
	public List<OnlineSizeResponseDTO> getOnlineSizeProfilesV2(OnlineSizeProfileRequestDTO onlineSizeProfileInput)
	{
		List<OnlineSizeResponseDTO> onlineSizeProfiles = new ArrayList<>();
		String season;
		String baseItemIds = null;
		String colorFamilyFinalStr = null;
		String seasonFinalStr = null;
		Long deptNbr = null;
		String year = null;
		try {
			baseItemIds = onlineSizeProfileInput.getBaseItemIds().stream().collect(Collectors.joining(","));
			colorFamilyFinalStr = getProperStringFormatForQuery(onlineSizeProfileInput.getColorFamilies());
			season = onlineSizeProfileInput.getSeason();
			seasonFinalStr = getProperStringFormatForQuery(season);
			deptNbr = onlineSizeProfileInput.getDeptNbr();
			year = onlineSizeProfileInput.getYear();

			String query = String.format(midasProperties.getOnlineSizeProfilesV2Query(), seasonFinalStr, deptNbr, baseItemIds, colorFamilyFinalStr, year);
			log.debug("Online size profile midas request: {}", query);
			String url = midasProperties.getMidasApiBaseURL();
			log.info("Invoking Midas API for Create event with URL: {} | query: {}", url, query);

			ResponseEntity<OnlineSizeProfileDataDTO> result =
					restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(query, getHeadersForMidas()), OnlineSizeProfileDataDTO.class);
			log.info("Successfully invoked Midas Api with result: {}", result);

			onlineSizeProfiles = Optional.ofNullable(result.getBody())
					.map(OnlineSizeProfileDataDTO::getPayload)
					.map(OnlineSizeProfilePayloadDTO::getResult)
					.map(OnlineSizeProfileResultDTO::getResponse)
					.orElse(new ArrayList<>());
			return onlineSizeProfiles;
		} catch (IllegalFormatConversionException ifce) {
			log.error("Online size profile input args invalid.  Skipping lookup.  season: {}, deptNbr: {}, baseItemId: {}, colorFamily: {}",
					seasonFinalStr, deptNbr, baseItemIds, colorFamilyFinalStr);
			throw new ClpApListenerException("Online size profile input args invalid");
		} catch (Exception e) {
			log.error("Exception in Getting Online profile data from Midas for the given Input: {}", e.getMessage());
			throw new ClpApListenerException("Exception in Getting Online Size profile");
		}
	}

	@Recover
	public List<OnlineSizeResponseDTO> recover(Exception e, OnlineSizeProfileRequestDTO onlineSizeProfileInput) {
		throw new ClpApListenerException("Online Size Profile max retries attempted: " + onlineSizeProfileInput.toString(), e);
	}

	@Retryable(backoff = @Backoff(delay = 10000))
	public List<StoreSizeResponseDTO> getStoreSizeProfiles(StoreSizeProfileRequestDTO storeSizeProfileInput) {
		List<StoreSizeResponseDTO> storeSizeProfiles = new ArrayList<>();
		String season;
		Long finelineNbr = null;
		String colorFamilyFinalStr = null;
		String seasonFinalStr = null;
		Long deptNbr = null;
		String year = null;

		try {
			finelineNbr = storeSizeProfileInput.getFinelineNbr();
			colorFamilyFinalStr = getProperStringFormatForQuery(storeSizeProfileInput.getColorFamily());
			season = storeSizeProfileInput.getSeason();
			seasonFinalStr = getProperStringFormatForQuery(season);
			deptNbr = storeSizeProfileInput.getDeptNbr();
			year = storeSizeProfileInput.getYear();

			String query = String.format(midasProperties.getStoreSizeProfilesQuery(), seasonFinalStr, deptNbr, finelineNbr, colorFamilyFinalStr, year);
			log.debug("Store size profile midas request: {}", query);
			String url = midasProperties.getMidasApiBaseURL();
			log.info("Invoking Midas API for Create event with URL: {} | query: {}", url, query);

			ResponseEntity<StoreSizeProfileDataDTO> result =
					restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(query, getHeadersForMidas()), StoreSizeProfileDataDTO.class);
			log.info("Successfully invoked Midas Api with result: {}", result);

			storeSizeProfiles = Optional.ofNullable(result.getBody())
					.map(StoreSizeProfileDataDTO::getPayload)
					.map(StoreSizeProfilePayloadDTO::getResult)
					.map(StoreSizeProfileResultDTO::getResponse)
					.orElse(new ArrayList<>());
			return storeSizeProfiles;
		} catch (IllegalFormatConversionException ifce) {
			log.error("Store size profile input args invalid.  Skipping lookup.  season: {}, deptNbr: {}, finelineNbr: {}, colorFamily: {}",
					seasonFinalStr, deptNbr, finelineNbr, colorFamilyFinalStr);
			throw new ClpApListenerException("Store size profile input args invalid");
		} catch (Exception e) {
			log.error("Exception in Getting Store profile data from Midas for the given Input: {}", e.getMessage());
			throw new ClpApListenerException("Exception in Getting Store profile data from Midas for the given Input");
		}
	}

	@Retryable(maxAttempts = 5, backoff = @Backoff(delay = 3000))
	public List<StoreSizeResponseDTO> getStoreSizeProfilesV2(StoreSizeProfileRequestDTO storeSizeProfileInput) {
		List<StoreSizeResponseDTO> storeSizeProfiles = new ArrayList<>();
		String season;
		Long finelineNbr = null;
		String colorFamilyFinalStr = null;
		String seasonFinalStr = null;
		Long deptNbr = null;
		Long catgNbr = null;
		Long subCatgNbr = null;
		String year = null;

		try {
			finelineNbr = storeSizeProfileInput.getFinelineNbr();
			colorFamilyFinalStr = getProperStringFormatForQuery(storeSizeProfileInput.getColorFamilies());
			season = storeSizeProfileInput.getSeason();
			seasonFinalStr = getProperStringFormatForQuery(season);
			deptNbr = storeSizeProfileInput.getDeptNbr();
			catgNbr = storeSizeProfileInput.getCatgNbr();
			subCatgNbr = storeSizeProfileInput.getSubCatgNbr();
			year = storeSizeProfileInput.getYear();

			String query = String.format(midasProperties.getStoreSizeProfilesV2Query(), seasonFinalStr, deptNbr, catgNbr, subCatgNbr, finelineNbr, colorFamilyFinalStr, year);
			log.debug("Store size profile midas request: {}", query);
			String url = midasProperties.getMidasApiBaseURL();
			log.info("Invoking Midas API for Create event with URL : {} and query : {}", url, query);

			ResponseEntity<StoreSizeProfileDataDTO> result =
					restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(query, getHeadersForMidas()), StoreSizeProfileDataDTO.class);
			log.info("Successfully invoked Midas Api with result = {}", result);

			storeSizeProfiles = Optional.ofNullable(result.getBody())
					.map(StoreSizeProfileDataDTO::getPayload)
					.map(StoreSizeProfilePayloadDTO::getResult)
					.map(StoreSizeProfileResultDTO::getResponse)
					.orElse(new ArrayList<>());
			return storeSizeProfiles;

		} catch (IllegalFormatConversionException ifce) {
			log.error("Store size profile input args invalid.  Skipping lookup.  season: {}, deptNbr: {}, finelineNbr: {}, colorFamily: {}",
					seasonFinalStr, deptNbr, finelineNbr, colorFamilyFinalStr);
			throw new ClpApListenerException("Store size profile input args invalid");
		} catch (Exception e) {
			log.error("Exception in Getting Store profile data from Midas for the given Input: {}", e.getMessage());
			throw new ClpApListenerException("Exception in Getting Store profile data from Midas for the given Input");
		}
	}

	@Recover
	public List<StoreSizeResponseDTO> recover(Exception e, StoreSizeProfileRequestDTO storeSizeProfileInput) {
		throw new ClpApListenerException("Store Size Profile max retries attempted: " + storeSizeProfileInput.toString(), e);
	}

    //If the input string is S3, then we have to convert it to 'S3' for Midas call to work
	private String getProperStringFormatForQuery(String input) {
		if (input != null && !input.isEmpty()) {
				StringBuilder stBuilder = new StringBuilder();
				stBuilder.append("'").append(input).append("'");
				return stBuilder.toString();
		}
		return input;
	}

	private String getProperStringFormatForQuery(Set<String> inputs) {
		if (inputs != null && !inputs.isEmpty()) {
			return inputs.stream().map(i -> {
				StringBuilder stBuilder = new StringBuilder();
				stBuilder.append("'").append(i).append("'");
				return stBuilder.toString();
			}).collect(Collectors.joining(","));
		}
		return "";
	}


}
