package com.example.hexagonal.adapter.in.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    private static final String SIGNUP_URL = "/api/auth/signup";
    private static final String LOGIN_URL = "/api/auth/login";
    private static final String LOGOUT_URL = "/api/auth/logout";
    private static final String PASSWORD = "Password1!";
    private static final String EMOJI = new String(Character.toChars(0x1F600));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 토큰_없이_회원가입하면_201과_회원_ID를_반환하고_비밀번호는_암호화되어_저장된다() throws Exception {
        String loginId = uniqueLoginId();

        signup(new SignupRequest(loginId, "Password1!", "홍길동", 25, loginId + "@example.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").isNumber())
                .andExpect(jsonPath("$.loginId").value(loginId));

        String storedPassword = jdbcTemplate.queryForObject(
                "SELECT password FROM members WHERE login_id = ?", String.class, loginId);
        assertThat(storedPassword).isNotEqualTo("Password1!");
        assertThat(passwordEncoder.matches("Password1!", storedPassword)).isTrue();
    }

    @Test
    void 이미_사용_중인_아이디로_가입하면_409_DUPLICATE_LOGIN_ID를_반환한다() throws Exception {
        String loginId = uniqueLoginId();
        signup(new SignupRequest(loginId, "Password1!", "홍길동", 25, loginId + "@example.com"))
                .andExpect(status().isCreated());

        signup(new SignupRequest(loginId, "Password1!", "김철수", 30, loginId + "-other@example.com"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("DUPLICATE_LOGIN_ID"));
    }

    @Test
    void 이미_사용_중인_이메일로_가입하면_409_DUPLICATE_EMAIL을_반환한다() throws Exception {
        String loginId = uniqueLoginId();
        String email = loginId + "@example.com";
        signup(new SignupRequest(loginId, "Password1!", "홍길동", 25, email))
                .andExpect(status().isCreated());

        signup(new SignupRequest(uniqueLoginId(), "Password1!", "김철수", 30, email))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
    }

    @Test
    void 입력값이_올바르지_않으면_400_INVALID_INPUT을_반환한다() throws Exception {
        signup(new SignupRequest(uniqueLoginId(), "password", "홍길동", 25, "valid@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message")
                        .value("비밀번호는 영문, 숫자, 특수문자를 각각 1자 이상 포함한 8~20자여야 하며 공백을 포함할 수 없습니다."));
    }

    @Test
    void 필수값이_누락되면_400_INVALID_INPUT을_반환한다() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void 요청_본문이_JSON_형식이_아니면_400_INVALID_INPUT을_반환한다() throws Exception {
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void 가입한_아이디와_비밀번호로_로그인하면_200과_Bearer_Access_Token을_반환한다() throws Exception {
        String loginId = signupMember();

        login(new LoginRequest(loginId, PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800));
    }

    @Test
    void 비밀번호가_틀리면_401_INVALID_CREDENTIALS를_반환한다() throws Exception {
        String loginId = signupMember();

        login(new LoginRequest(loginId, "Wrong1234!"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void 존재하지_않는_아이디로_로그인하면_401_INVALID_CREDENTIALS를_반환한다() throws Exception {
        login(new LoginRequest(uniqueLoginId(), PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void 로그인_필수값이_누락되면_400_INVALID_INPUT을_반환한다() throws Exception {
        login(new LoginRequest(uniqueLoginId(), ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("비밀번호는 필수입니다."));
    }

    @Test
    void 이모지가_포함된_비밀번호로_가입하면_같은_비밀번호로_로그인할_수_있다() throws Exception {
        // code point 12자, UTF-16 길이 21자
        String password = "a1!" + EMOJI.repeat(9);
        String loginId = uniqueLoginId();
        signup(new SignupRequest(loginId, password, "홍길동", 25, loginId + "@example.com"))
                .andExpect(status().isCreated());

        login(new LoginRequest(loginId, password))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void 로그인_비밀번호가_code_point_기준_20자를_넘으면_400_INVALID_INPUT을_반환한다() throws Exception {
        login(new LoginRequest(uniqueLoginId(), "a1!" + EMOJI.repeat(18)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("비밀번호는 20자 이하여야 합니다."));
    }

    @Test
    void 비밀번호가_UTF_8_기준_72바이트이면_가입과_로그인에_성공한다() throws Exception {
        // code point 20자, UTF-8 72바이트
        String password = "a1" + EMOJI.repeat(17) + "\u00E9";
        String loginId = uniqueLoginId();
        signup(new SignupRequest(loginId, password, "홍길동", 25, loginId + "@example.com"))
                .andExpect(status().isCreated());

        login(new LoginRequest(loginId, password))
                .andExpect(status().isOk());
    }

    @Test
    void 비밀번호가_UTF_8_기준_72바이트를_넘으면_가입할_수_없다() throws Exception {
        // code point 20자, UTF-8 74바이트
        String loginId = uniqueLoginId();
        signup(new SignupRequest(loginId, EMOJI.repeat(18) + "a1", "홍길동", 25, loginId + "@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("비밀번호는 UTF-8 기준 72바이트 이하여야 합니다."));
    }

    @Test
    void 로그인_비밀번호가_UTF_8_기준_72바이트를_넘으면_400_INVALID_INPUT을_반환한다() throws Exception {
        login(new LoginRequest(uniqueLoginId(), EMOJI.repeat(18) + "a1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("비밀번호는 UTF-8 기준 72바이트 이하여야 합니다."));
    }

    @Test
    void 로그아웃하면_204를_반환하고_같은_토큰은_더_이상_사용할_수_없다() throws Exception {
        String accessToken = loginAndGetAccessToken(signupMember());

        logout(accessToken)
                .andExpect(status().isNoContent());

        logout(accessToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REVOKED_TOKEN"));
    }

    @Test
    void 로그아웃해도_새로_로그인해_발급받은_토큰은_사용할_수_있다() throws Exception {
        String loginId = signupMember();
        logout(loginAndGetAccessToken(loginId))
                .andExpect(status().isNoContent());

        logout(loginAndGetAccessToken(loginId))
                .andExpect(status().isNoContent());
    }

    @Test
    void 토큰_없이_로그아웃하면_401_UNAUTHORIZED를_반환한다() throws Exception {
        mockMvc.perform(post(LOGOUT_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private String signupMember() throws Exception {
        String loginId = uniqueLoginId();
        signup(new SignupRequest(loginId, PASSWORD, "홍길동", 25, loginId + "@example.com"))
                .andExpect(status().isCreated());
        return loginId;
    }

    private String loginAndGetAccessToken(String loginId) throws Exception {
        String body = login(new LoginRequest(loginId, PASSWORD))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private ResultActions login(LoginRequest request) throws Exception {
        return mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions logout(String accessToken) throws Exception {
        return mockMvc.perform(post(LOGOUT_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    private ResultActions signup(SignupRequest request) throws Exception {
        return mockMvc.perform(post(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private String uniqueLoginId() {
        return "t" + ThreadLocalRandom.current().nextLong(100_000_000L, 1_000_000_000L);
    }
}
