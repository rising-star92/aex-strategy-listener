package com.walmart.aex.strategy.listener.properties;

import io.strati.ccm.utils.client.annotation.Configuration;
import io.strati.ccm.utils.client.annotation.Property;

@Configuration(configName = "midasConfig")
public interface MidasApiProperties {

    @Property(propertyName = "midas.baseURL")
    String getMidasApiBaseURL();

    @Property(propertyName = "midas.header.contentType")
    String getMidasHeaderContentType();

    @Property(propertyName = "midas.header.consumer")
    String getMidasHeaderConsumer();

    @Property(propertyName = "midas.header.authorization")
    String getMidasHeaderAuthorization();

    @Property(propertyName = "midas.header.signatureKeyVersion")
    String getMidasHeaderSignatureKeyVersion();

    @Property(propertyName = "midas.header.signatureAuthFlag")
    String getMidasHeaderSignatureAuthFlag();

    @Property(propertyName = "midas.header.signatureTS")
    String getMidasHeaderSignatureTS();

    @Property(propertyName = "midas.header.tenant")
    String getMidasHeaderTenant();

    @Property(propertyName = "midas.clickhouse.table")
    String getMidasClickhouseTable();

    @Property(propertyName = "midas.apRankingMetricsQuery")
    String getAPRankingMetricsQuery();

    @Property(propertyName = "midas.spGetStoreSizeProfilesQuery_v2")
    String getStoreSizeProfilesV2Query();

    @Property(propertyName = "midas.spGetStoreSizeProfilesQuery")
    String getStoreSizeProfilesQuery();

    @Property(propertyName = "midas.spGetOnlineSizeProfilesQuery")
    String getOnlineSizeProfilesQuery();

    @Property(propertyName = "midas.spGetOnlineSizeProfilesQuery_v2")
    String getOnlineSizeProfilesV2Query();
}
