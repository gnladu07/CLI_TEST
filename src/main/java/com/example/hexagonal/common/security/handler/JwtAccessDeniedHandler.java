package com.example.hexagonal.common.security.handler;

import com.example.hexagonal.common.security.SecurityErrorCode;
import com.example.hexagonal.common.security.SecurityErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorResponseWriter errorResponseWriter;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        log.warn(
                "JwtAccessDeniedHandler : handle : 권한 없는 요청 차단 - method={}, uri={}",
                request.getMethod(),
                request.getRequestURI()
        );

        errorResponseWriter.write(response, SecurityErrorCode.ACCESS_DENIED);
    }
}
