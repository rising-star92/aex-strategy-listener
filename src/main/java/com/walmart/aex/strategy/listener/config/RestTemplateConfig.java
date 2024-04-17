package com.walmart.aex.strategy.listener.config;


import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.net.ssl.SSLContext;

import com.walmart.aex.strategy.listener.properties.HttpConnectionProperties;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import io.strati.ccm.utils.client.annotation.ManagedConfiguration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RestTemplateConfig {

    @ManagedConfiguration
    private HttpConnectionProperties httpConnectionProperties;

    @Bean
    @Primary
    public RestTemplate getRestTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory();
        if (requestFactory == null)
            return null;

        requestFactory.setReadTimeout(httpConnectionProperties.getReadTimeout());
        requestFactory.setConnectTimeout(httpConnectionProperties.getConnectTimeout());
        requestFactory.setConnectionRequestTimeout(httpConnectionProperties.getConnectionRequestTimeout());
        return new RestTemplate(requestFactory);

    }

    @Bean("restTemplateWithShortTimeout")
    public RestTemplate getRestTemplateWithShortTimeout() {
        HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory();
        if (requestFactory == null)
            return null;

        requestFactory.setReadTimeout(httpConnectionProperties.getShortReadTimeout());
        requestFactory.setConnectTimeout(httpConnectionProperties.getShortConnectTimeout());
        requestFactory.setConnectionRequestTimeout(httpConnectionProperties.getShortConnectionRequestTimeout());
        return new RestTemplate(requestFactory);
    }

    private HttpComponentsClientHttpRequestFactory getRequestFactory() {
        try {
            TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

            requestFactory.setHttpClient(httpClient);

            return requestFactory;
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            log.error("Error Occurred in Rest Template Configuration - " + Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

}
