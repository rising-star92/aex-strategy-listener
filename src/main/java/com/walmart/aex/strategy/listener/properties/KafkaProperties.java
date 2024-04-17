package com.walmart.aex.strategy.listener.properties;

import io.strati.ccm.utils.client.annotation.Configuration;
import io.strati.ccm.utils.client.annotation.Property;

import java.util.List;

@Configuration(configName = "kafkaConfig")
public interface KafkaProperties {

    @Property(propertyName = "aex.clp.ap.kafka.server")
    String getListenerKafkaServer();

    @Property(propertyName = "aex.clp.ap.kafka.topic")
    String getListenerKafkaTopic();

    @Property(propertyName = "aex.plan.definition.kafka.topic")
    String getCBAMKafkaTopic();

    @Property(propertyName = "aex.clp.ap.kafka.consumer.group-id")
    String getListenerGroupId();

    @Property(propertyName ="aex.plandefinition.ap.kafka.consumer.group-id")
    String getPlanDefinitionApListenerGroupId();

    @Property(propertyName = "kafka.sessionTimeout")
    String getSessionTimeoutConfig();

    @Property(propertyName = "kafka.heartbeatInterval")
    String getHeartbeatIntervalConfig();

    @Property(propertyName = "kafka.autoOffsetReset")
    String getAutoOffsetResetConfig();

    @Property(propertyName = "kafka.enableAutoCommit")
    String getEnableAutoCommitConfig();

    @Property(propertyName = "kafka.consumer.accepted.types")
    List<String> getKafkaConsumerAcceptedTypes();

    @Property(propertyName = "kafka.consumer.accepted.channels")
    List<String> getKafkaConsumerAcceptedChannels();

    @Property(propertyName = "kafka.consumer.accepted.updates")
    List<String> getKafkaConsumerAcceptedUpdates();

    @Property(propertyName = "kafka.consumer.initial.load.flag")
    String getKafkaConsumerInitialLoadFlag();

    @Property(propertyName = "kafka.consumer.assort.product.flag")
    String getKafkaConsumerAssortProductFlag();

    @Property(propertyName = "kafka.consumer.size.pack.flag")
    String getKafkaConsumerSizePackFlag();

}
