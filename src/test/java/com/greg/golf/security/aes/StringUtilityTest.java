package com.greg.golf.security.aes;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@Slf4j
class StringUtilityTest {


    @DisplayName("Should encrypt and decrypt given string")
    @Test
    void encryptDecryptStringTest() throws Exception{

        var testString = "plainText12$";
        var password = "secret";

        try{
            var encryptedString = StringUtility.encryptString(testString, password);
            log.info("Encrypted string: " + encryptedString);
            var decryptedString = StringUtility.decryptString(encryptedString, password);
            Assertions.assertEquals(testString, decryptedString);

        } catch (Exception e) {
            Assertions.fail("Should not have thrown any exception");
        }
    }
}