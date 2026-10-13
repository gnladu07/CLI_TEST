package com.example.hexagonal.common.security.handler;

import com.example.hexagonal.common.security.SecurityErrorCode;
import com.example.hexagonal.common.security.SecurityErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        SecurityErrorCode errorCode = resolveErrorCode(request);

        log.warn(
                "JwtAuthenticationEntryPoint : commence : 인증 실패 요청 차단 - code={}, method={}, uri={}",
                errorCode,
                request.getMethod(),
                request.getRequestURI()
        );

        errorResponseWriter.write(response, errorCode);
    }

    private SecurityErrorCode resolveErrorCode(HttpServletRequest request) {
        Object attribute = request.getAttribute(SecurityErrorCode.REQUEST_ATTRIBUTE);
        if (attribute instanceof SecurityErrorCode errorCode) {
            return errorCode;
        }
        return SecurityErrorCode.UNAUTHORIZED;
    }
}
