package com.walmart.aex.strategy.listener.properties;

import io.strati.ccm.utils.client.annotation.Configuration;
import io.strati.ccm.utils.client.annotation.Property;

@Configuration(configName = "featureConfig")
public interface FeatureConfigProperties {

    @Property(propertyName = "enable.deletePlanCategorySubCategory")
    Boolean isEnableDeletePlanCategorySubCategory();

    @Property(propertyName = "likeFinelineFeatureFlag")
    Boolean isLikeFinelineFeatureFlag();

    @Property(propertyName = "kafka.errorHandling.enabled")
    Boolean isErrorHandlingEnabled();

}
