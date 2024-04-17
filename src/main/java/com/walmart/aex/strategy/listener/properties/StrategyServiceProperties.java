package com.walmart.aex.strategy.listener.properties;

import io.strati.ccm.utils.client.annotation.Configuration;
import io.strati.ccm.utils.client.annotation.Property;

import java.util.List;

@Configuration(configName = "strategyConfig")
public interface StrategyServiceProperties {

    @Property(propertyName = "strategy.baseUrl")
    String getStrategyApiBaseURL();

    @Property(propertyName = "strategy.accepted.fixture.updates")
    List<String> getAcceptedFixtureStrategyUpdates();

    @Property(propertyName = "strategy.consumerId")
    String getStrategyConsumerId();

    @Property(propertyName = "strategy.appKey")
    String getStrategyAppKey();

    @Property(propertyName = "strategy.env")
    String getStrategyEnv();
}
