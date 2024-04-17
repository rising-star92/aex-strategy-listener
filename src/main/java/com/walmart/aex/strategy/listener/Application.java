package com.walmart.aex.strategy.listener;

import io.strati.ccm.utils.client.logger.LogLevel;
import io.strati.ccm.utils.client.logger.Logger;
import io.strati.ccm.utils.client.logger.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.retry.annotation.EnableRetry;

import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.walmart.platform.txn.springboot.filters")
@ComponentScan("com.walmart.platform.txn.springboot.interceptor")
@SpringBootApplication(scanBasePackages = {"com.walmart.aex.strategy.listener",
        "io.strati.tunr.utils.client"}, exclude = KafkaAutoConfiguration.class)
@EnableRetry
public class Application {
    static Logger logger = LoggerFactory.getInstance().getLogger();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        logger.setLogLevel(LogLevel.ERROR);
    }
}
