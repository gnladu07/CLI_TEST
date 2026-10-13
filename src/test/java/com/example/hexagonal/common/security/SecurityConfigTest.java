package com.example.hexagonal.common.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.hexagonal.common.security.jwt.JwtTokenProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecurityConfigTest {

    private static final String PROTECTED_URL = "/api/protected-resource";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void 토큰_없이_보호된_URL에_접근하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(get(PROTECTED_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void 잘못된_토큰으로_접근하면_401_INVALID_TOKEN을_반환한다() throws Exception {
        mockMvc.perform(get(PROTECTED_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void 유효한_토큰이면_인증을_통과한다() throws Exception {
        String token = jwtTokenProvider.createAccessToken("member-uuid", List.of("ROLE_USER"));

        // 매핑된 Controller가 없으므로 인증을 통과하면 404가 반환된다.
        mockMvc.perform(get(PROTECTED_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void 공개_URL은_토큰_없이_접근할_수_있다() throws Exception {
        // 로그인은 POST만 매핑되어 있으므로 인증을 통과하면 405가 반환된다.
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void 공개_URL로_지정되지_않은_인증_API는_토큰이_필요하다() throws Exception {
        mockMvc.perform(get("/api/auth/anything"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
