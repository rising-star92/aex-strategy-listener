package com.walmart.aex.strategy.listener.properties;

import io.strati.ccm.utils.client.annotation.Configuration;
import io.strati.ccm.utils.client.annotation.Property;

@Configuration(configName = "ahsApiConfig")
public interface AHSApiProperties {

    @Property(propertyName = "strategy.sizeUrl")
    String getAHSSizeURL();

    @Property(propertyName = "strategy.sizeCustomerID")
    String getAHSHeaderCustomerID();

    @Property(propertyName = "strategy.sizeServiceName")
    String getAHSHeaderServiceName();

    @Property(propertyName = "strategy.sizeApiEnv")
    String getAHSHeaderEnvironment();

    @Property(propertyName = "strategy.sizeAttributeName")
    String getAHSHeaderSizeAttribute();

    @Property(propertyName = "size.pack.release.flag")
    String getSizePackReleaseFlag();
}
