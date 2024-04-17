package com.walmart.aex.strategy.listener.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CommonUtilsTest {

    @Test
    void testRemoveDoubleQuotes_EmptyString(){
        String result = CommonUtils.removeDoubleQuotes("");
        Assert.assertEquals("", result);
    }

    @Test
    void testRemoveDoubleQuotes_Null(){
        String result = CommonUtils.removeDoubleQuotes(null);
        Assert.assertEquals("", result);
    }

    @Test
    void testRemoveDoubleQuotes_EmptyStringWithSpaces(){
        String result = CommonUtils.removeDoubleQuotes("    ");
        Assert.assertEquals("", result);
    }

    @Test
    void testRemoveDoubleQuotes_FromActualMessage() throws IOException {
        String message =   new String(Files.readAllBytes(Paths.get("src/test/resources/clpMessageCreate.txt")));;
        String result = CommonUtils.removeDoubleQuotes(message);
        Assert.assertEquals(true, result.length()>0);
    }


}
