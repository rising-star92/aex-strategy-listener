package com.walmart.aex.strategy.listener.config;

import com.walmart.aex.kafka.common.exception.AexKafkaListenerErrorHandler;
import com.walmart.aex.kafka.common.interceptor.AexKafkaErrorHandlingInterceptor;
import com.walmart.aex.strategy.listener.properties.FeatureConfigProperties;
import com.walmart.aex.strategy.listener.properties.KafkaProperties;
import com.walmart.aex.strategy.listener.properties.CredProperties;
import com.walmart.aex.strategy.listener.utils.ListenerMessageUtil;
import io.strati.ccm.utils.client.annotation.ManagedConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Configuration
@Slf4j
@EnableKafka
public class KafkaConfig {

    @ManagedConfiguration
    private KafkaProperties kafkaProperties;

    @ManagedConfiguration
    private FeatureConfigProperties featureConfigProperties;

    @Autowired
    private ListenerMessageUtil listenerMessageUtil;

    @Autowired(required = false)
    private AexKafkaErrorHandlingInterceptor aexKafkaErrorHandlingInterceptor;

    @Autowired
    private AexKafkaListenerErrorHandler aexKafkaListenerErrorHandler;

    private CredProperties credProperties;

    @Value("${aex.errorHandler.enabled:true}")
    private Boolean isErrorHandlerEnabled;

    public KafkaConfig(CredProperties credProperties) {
        this.credProperties = credProperties;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getListenerKafkaServer());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaProperties.getSessionTimeoutConfig());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaProperties.getHeartbeatIntervalConfig());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getAutoOffsetResetConfig());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.getEnableAutoCommitConfig());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, CooperativeStickyAssignor.class.getName());
        props.put("security.protocol", "SSL");
        props.put("ssl.truststore.location", getTrustStoreFileLocation());
        props.put("ssl.truststore.password", credProperties.getTrustStoreFilePassword());
        props.put("ssl.keystore.location", getKeyStoreFileLocation());
        props.put("ssl.keystore.password", credProperties.getTrustStoreFilePassword());
        props.put("ssl.key.password", credProperties.getTrustStoreFilePassword());
        props.put("ssl.endpoint.identification.algorithm", "");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(String groupId) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(groupId));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setAckDiscarded(true);
        factory.setRecordFilterStrategy(this::isMessageNotEligibleForAP);
        if(isErrorHandlerEnabled && featureConfigProperties.isErrorHandlingEnabled()){
            factory.setRecordInterceptor(aexKafkaErrorHandlingInterceptor);
            factory.setErrorHandler(aexKafkaListenerErrorHandler);
        }
        return factory;
    }


    private boolean isMessageNotEligibleForAP(ConsumerRecord<String, String> consumerRecord) {
        return listenerMessageUtil.isMessageNotEligibleForAP(consumerRecord);
    }

    @Bean
    @Profile("!test")
    public ConcurrentKafkaListenerContainerFactory<String, String> planDefinitionListenerContainerFactory() {
        return kafkaListenerContainerFactory(kafkaProperties.getPlanDefinitionApListenerGroupId());
    }

    @Bean
    @Profile("!test")
    public ConcurrentKafkaListenerContainerFactory<String, String> lpapListenerContainerFactory() {
        return kafkaListenerContainerFactory(kafkaProperties.getListenerGroupId());
    }

    private String getTrustStoreFileLocation() {
        String truststoreFileName = "/tmp/kafkatruststore.jks";

        File trustStoreFile = new File(truststoreFileName);
        log.info("Truststore File Location: {}", truststoreFileName);
        try (FileOutputStream fos = new FileOutputStream(trustStoreFile)) {
            String truststore = new String(Files.readAllBytes(Paths.get("/etc/secrets/ssl.truststore.txt")));
            fos.write(Base64.getDecoder().decode(truststore));
        } catch (Exception e) {
            log.error(
                    "error writing file: {} | {} | {}",
                    e.getClass().getCanonicalName(),
                    e.getMessage(),
                    e.getCause());
            return "";
        }
        return truststoreFileName;
    }

    private String getKeyStoreFileLocation() {
        String keystoreFileName = "/tmp/kafkakeystore.jks";

        File keystoreFile = new File(keystoreFileName);
        log.info("Truststore File Location: {}", keystoreFileName);
        try (FileOutputStream fos = new FileOutputStream(keystoreFile)) {
            String keystore = new String(Files.readAllBytes(Paths.get("/etc/secrets/ssl.keystore.txt")));
            fos.write(Base64.getDecoder().decode(keystore));
        } catch (Exception e) {
            log.error(
                    "error writing file: {} | {} | {}",
                    e.getClass().getCanonicalName(),
                    e.getMessage(),
                    e.getCause());
            return "";
        }
        return keystoreFileName;
    }
}
