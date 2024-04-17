package com.walmart.aex.strategy.listener.properties;

import io.strati.ccm.utils.client.annotation.Configuration;
import io.strati.ccm.utils.client.annotation.Property;

import java.util.List;

@Configuration(configName = "sizePackConfig")
public interface SizePackProperties {
    @Property(propertyName = "size.pack.accepted.updates")
    List<String> getAcceptedSizePackUpdates();

    @Property(propertyName = "size.pack.plan.desc.feature.flag")
    boolean isDefaultPlanDesc();

    @Property(propertyName = "size.pack.plan.desc.value")
    String getDefaultPlanDesc();
}
