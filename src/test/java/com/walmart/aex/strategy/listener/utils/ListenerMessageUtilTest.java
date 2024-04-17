package com.walmart.aex.strategy.listener.utils;

import com.walmart.aex.strategy.listener.properties.KafkaProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
class ListenerMessageUtilTest {
    @InjectMocks
    ListenerMessageUtil listenerMessageUtil;

    @Mock
    KafkaProperties kafkaProperties;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        Field field2 = ReflectionUtils.findField(ListenerMessageUtil.class, "kafkaProperties");
        field2.setAccessible(true);
        field2.set(listenerMessageUtil, kafkaProperties);

        List<String> types = new ArrayList<>();
        types.add("create");
        types.add("update");
        types.add("delete");

        List<String> channels = new ArrayList<>();
        channels.add("online");
        channels.add("store");
        channels.add("omni");

        List<String> updates = new ArrayList<>();
        updates.add("channel");

        Mockito.lenient().when(kafkaProperties.getKafkaConsumerAcceptedTypes()).thenReturn(types);
        Mockito.lenient().when(kafkaProperties.getKafkaConsumerAcceptedChannels()).thenReturn(channels);
        Mockito.lenient().when(kafkaProperties.getKafkaConsumerAcceptedUpdates()).thenReturn(updates);
    }

    @Test
    void isMessageNotEligibleForAPTrueTest() throws IOException {
        String message = readTextFileAsString("clpMessageCreate");
        ConsumerRecord<String, String> record = new ConsumerRecord<String, String>("topic", 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0,"1", message);
        assertFalse(listenerMessageUtil.isMessageNotEligibleForAP(record));
    }

    @Test
    void isMessageNotEligibleForAPFalseTest() throws IOException {
        String message = readTextFileAsString("clpMessageCreateFalse");
        ConsumerRecord<String, String> record = new ConsumerRecord<String, String>("topic", 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0,"1", message);
        assertTrue(listenerMessageUtil.isMessageNotEligibleForAP(record));
    }

    @Test
    void isMessageNotEligibleForAPFalseTest2() throws IOException {
        ConsumerRecord<String, String> record = new ConsumerRecord<String, String>("topic", 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0,"1", "exceptionText");
        assertTrue(listenerMessageUtil.isMessageNotEligibleForAP(record));
    }

    @Test
    void isMessageNotEligibleForLinePlanTrueTest() throws IOException {
        String message = readTextFileAsString("clpMessageUpdate");
        ConsumerRecord<String, String> record = new ConsumerRecord<String, String>("topic", 1, 0, 0L, TimestampType.CREATE_TIME, 0L, 0, 0, "1", message);
        assertTrue(listenerMessageUtil.isMessageNotEligibleForAP(record));
    }


    private String readTextFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/" + fileName + ".txt")));
    }
}
