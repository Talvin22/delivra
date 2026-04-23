package site.delivra.application.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilsTest {

    @Test
    void isNotValidPassword_nullPassword_returnsTrue() {
        assertTrue(PasswordUtils.isNotValidPassword(null));
    }

    @Test
    void isNotValidPassword_emptyPassword_returnsTrue() {
        assertTrue(PasswordUtils.isNotValidPassword(""));
    }

    @Test
    void isNotValidPassword_whitespacePassword_returnsTrue() {
        assertTrue(PasswordUtils.isNotValidPassword("   "));
    }

    @Test
    void isNotValidPassword_tooShort_returnsTrue() {
        assertTrue(PasswordUtils.isNotValidPassword("Aa1!"));
    }

    @Test
    void isNotValidPassword_noUpperCase_returnsTrue() {
        assertTrue(PasswordUtils.isNotValidPassword("abcdef1!"));
    }

    @Test
    void isNotValidPassword_noLowerCase_returnsTrue() {
        assertTrue(PasswordUtils.isNotValidPassword("ABCDEF1!"));
    }

    @Test
    void isNotValidPassword_noDigit_returnsTrue() {
        assertTrue(PasswordUtils.isNotValidPassword("Abcdefgh!"));
    }

    @Test
    void isNotValidPassword_noSpecialCharacter_returnsTrue() {
        assertTrue(PasswordUtils.isNotValidPassword("Abcdefg1"));
    }

    @Test
    void isNotValidPassword_containsForbiddenCharacter_returnsTrue() {
        assertTrue(PasswordUtils.isNotValidPassword("Abcdef1!й"));
    }

    @Test
    void isNotValidPassword_validPassword_returnsFalse() {
        assertFalse(PasswordUtils.isNotValidPassword("Abcdef1!"));
    }

    @Test
    void isNotValidPassword_complexValidPassword_returnsFalse() {
        assertFalse(PasswordUtils.isNotValidPassword("MyStr0ng@Pass"));
    }

    @Test
    void isNotValidPassword_trimmedValidPassword_returnsFalse() {
        assertFalse(PasswordUtils.isNotValidPassword("  Abcdef1!  "));
    }
}