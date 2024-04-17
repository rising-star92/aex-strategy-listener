package com.walmart.aex.strategy.listener.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.walmart.aex.kafka.common.properties.KafkaRedirectProperties;
import com.walmart.aex.strategy.listener.exception.ClpApListenerException;
import com.walmart.aex.strategy.listener.properties.KafkaProperties;
import com.walmart.aex.strategy.listener.service.APConsumerService;
import com.walmart.aex.strategy.listener.service.PlanDefinitionService;
import io.strati.ccm.utils.client.annotation.ManagedConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.event.ListenerContainerIdleEvent;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@DependsOn({"lpapListenerContainerFactory", "planDefinitionListenerContainerFactory"})
public class Listener {

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @ManagedConfiguration
    public KafkaProperties kafkaProperties;

    @ManagedConfiguration
    public KafkaRedirectProperties kafkaRedirectProperties;

    @Autowired
    private APConsumerService apConsumerService;

    @Autowired
    private PlanDefinitionService planDefinitionService;


    @KafkaListener(topics = "#{__listener.kafkaProperties.getListenerKafkaTopic()}", id = "#{__listener.kafkaProperties.getListenerGroupId()}",
            containerFactory = "lpapListenerContainerFactory" , autoStartup = "false")
    public void apConsumer(@Payload String message) throws JsonProcessingException {
            log.info("Received message from CLP : {}", message);
            apConsumerService.processMessage(message);
            log.info("Message processing has been completed for : {}", message);
    }

    @KafkaListener(topics = "#{__listener.kafkaProperties.getCBAMKafkaTopic()}", id = "#{__listener.kafkaProperties.getPlanDefinitionApListenerGroupId()}",
            containerFactory = "planDefinitionListenerContainerFactory", autoStartup = "false")
    public void planDefinitionConsumer(@Payload String message) throws ClpApListenerException {
            log.info("Received message from PlanDefinition : {}", message);
            planDefinitionService.processMessage(message);
            log.info("Message processing has been completed for : {}", message);

    }

    @EventListener
    public void eventHandler(ListenerContainerIdleEvent event) {
        KafkaConsumer consumer = (KafkaConsumer) event.getConsumer();
        String groupId = consumer.groupMetadata().groupId();
        if (groupId.startsWith(kafkaRedirectProperties.getAexKafkaRedirectGroupId()))
        {
            if(!kafkaListenerEndpointRegistry.getListenerContainer(kafkaProperties.getListenerGroupId()).isRunning()) {
                kafkaListenerEndpointRegistry.getListenerContainer(kafkaProperties.getListenerGroupId()).start();
                log.info("Kafka Consumer for LP to AP Started..");
            }

            if (!kafkaListenerEndpointRegistry.getListenerContainer(kafkaProperties.getPlanDefinitionApListenerGroupId()).isRunning()) {
                kafkaListenerEndpointRegistry.getListenerContainer(kafkaProperties.getPlanDefinitionApListenerGroupId()).start();
                log.info("Kafka Consumer for Plan Definition to AP Started..");
            }
        }
    }
}
