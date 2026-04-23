package site.delivra.application.utils;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import site.delivra.application.security.DelivraUserDetails;
import site.delivra.application.security.JwtTokenProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiUtilsTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private ApiUtils apiUtils;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMethodName_returnsNonEmptyName() {
        String name = ApiUtils.getMethodName();
        assertNotNull(name);
        assertFalse(name.isBlank());
    }

    @Test
    void createAuthCookie_setsExpectedAttributes() {
        Cookie cookie = ApiUtils.createAuthCookie("Bearer xyz");

        assertEquals(HttpHeaders.AUTHORIZATION, cookie.getName());
        assertEquals("Bearer xyz", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(60 * 60 * 24 * 30, cookie.getMaxAge());
    }

    @Test
    void generateUuidWithoutDash_returns32HexChars() {
        String uuid = ApiUtils.generateUuidWithoutDash();
        assertEquals(32, uuid.length());
        assertFalse(uuid.contains("-"));
    }

    @Test
    void generateUuidWithoutDash_subsequentCallsAreUnique() {
        String a = ApiUtils.generateUuidWithoutDash();
        String b = ApiUtils.generateUuidWithoutDash();
        assertFalse(a.equals(b));
    }

    @Test
    void getCurrentUsername_returnsAuthenticationName() {
        var auth = new UsernamePasswordAuthenticationToken("user@example.com", "creds", List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("user@example.com", ApiUtils.getCurrentUsername());
    }

    @Test
    void getUserIdFromAuthentication_parsesTokenAndReturnsUserId() {
        var auth = new UsernamePasswordAuthenticationToken("user", "jwt-token", List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(jwtTokenProvider.getUserId("jwt-token")).thenReturn("42");

        assertEquals(42, apiUtils.getUserIdFromAuthentication());
    }

    @Test
    void getCompanyIdFromAuthentication_withDelivraUserDetails_returnsCompanyId() {
        var auth = new UsernamePasswordAuthenticationToken("user", "creds", List.of());
        auth.setDetails(new DelivraUserDetails(1, 100));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(100, apiUtils.getCompanyIdFromAuthentication());
    }

    @Test
    void getCompanyIdFromAuthentication_withoutDetails_returnsNull() {
        var auth = new UsernamePasswordAuthenticationToken("user", "creds", List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertNull(apiUtils.getCompanyIdFromAuthentication());
    }

    @Test
    void getCompanyIdFromAuthentication_noAuthentication_returnsNull() {
        assertNull(apiUtils.getCompanyIdFromAuthentication());
    }
}
