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
    void encryptDecryptStringTest() {

        var testString = "plainText12$";
        var password = "secret";

        Assertions.assertDoesNotThrow(() -> {
            var encryptedString = StringUtility.encryptString(testString, password);
            log.info("Encrypted string: " + encryptedString);
            var decryptedString = StringUtility.decryptString(encryptedString, password);
            Assertions.assertEquals(testString, decryptedString);
        });
    }
}