package lv.studymanager.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
            "test-secret-key-that-is-at-least-256-bits-long-for-testing-only-12345");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 900000L);
        ReflectionTestUtils.setField(tokenProvider, "refreshExpirationMs", 604800000L);
    }

    @Test
    void generateAccessToken_andValidate_success() {
        String token = tokenProvider.generateAccessToken("test@test.lv");

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void generateRefreshToken_andValidate_success() {
        String token = tokenProvider.generateRefreshToken("test@test.lv");

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void getEmailFromToken_returnsCorrectEmail() {
        String token = tokenProvider.generateAccessToken("test@test.lv");

        assertThat(tokenProvider.getEmailFromToken(token)).isEqualTo("test@test.lv");
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertThat(tokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void validateToken_emptyToken_returnsFalse() {
        assertThat(tokenProvider.validateToken("")).isFalse();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredProvider, "jwtSecret",
            "test-secret-key-that-is-at-least-256-bits-long-for-testing-only-12345");
        ReflectionTestUtils.setField(expiredProvider, "jwtExpirationMs", -1000L);
        ReflectionTestUtils.setField(expiredProvider, "refreshExpirationMs", -1000L);

        String token = expiredProvider.generateAccessToken("test@test.lv");

        assertThat(tokenProvider.validateToken(token)).isFalse();
    }
}
