package com.walmart.aex.strategy.listener.properties;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@Slf4j
public class CredProperties {

    private static final String LOCAL = "local";
    private static final String MIDAS_AUTH_LOC = "/secrets/midasApi.authorization.txt";
    private static final String TRUST_STORE_LOC = "/etc/secrets/ssl.truststore.password.txt";

    @Value("#{systemProperties['midasApi.authorization']}")
    private String midasAPIAuthorization;

    @Value("#{systemProperties['ssl.truststore.password']}")
    private String sslTrustStorePassword;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    public String fetchMidasApiAuthorization() {
        String midasAuth = null;
        try {
            midasAuth = activeProfile.contains(LOCAL) ? midasAPIAuthorization : new String(Files.readAllBytes(Paths.get("/etc" +
                    MIDAS_AUTH_LOC)));
        } catch (IOException e) {
            log.error("Error reading midas api authorization" + e.getMessage());
        }
        return midasAuth;
    }

    public Object getTrustStoreFilePassword() {
        String trustStorePassword = null;
        try {
            trustStorePassword = activeProfile.contains(LOCAL) ? sslTrustStorePassword :
                    new String(Files.readAllBytes(Paths.get(TRUST_STORE_LOC)));
        } catch (IOException e) {
            log.error("Error reading truststore password" + e.getMessage());
        }
        return trustStorePassword;
    }
}
