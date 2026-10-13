package com.example.hexagonal.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 404, 405 등 Spring MVC 표준 예외는 ResponseEntityExceptionHandler가 원래 상태 코드로 처리하고,
 * 응답 본문만 ErrorResponse 형식으로 통일한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("GlobalExceptionHandler : handleBusinessException : 비즈니스 예외 발생 - code={}", errorCode);
        return toResponse(errorCode, errorCode.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("GlobalExceptionHandler : handleException : 처리되지 않은 예외 발생", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse(ErrorCode.INVALID_INPUT.getMessage());

        log.warn(
                "GlobalExceptionHandler : handleMethodArgumentNotValid : 입력값 검증 실패 - fields={}",
                ex.getBindingResult().getFieldErrors().stream().map(FieldError::getField).toList()
        );

        return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
                .body(toBody(ErrorCode.INVALID_INPUT, message));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        log.warn("GlobalExceptionHandler : handleHttpMessageNotReadable : 요청 본문 해석 실패");
        return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
                .body(toBody(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String code = status != null ? status.name() : String.valueOf(statusCode.value());
        String message = status != null ? status.getReasonPhrase() : code;

        ErrorResponse errorResponse = new ErrorResponse(statusCode.value(), code, message);
        return super.handleExceptionInternal(ex, errorResponse, headers, statusCode, request);
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatus()).body(toBody(errorCode, message));
    }

    private ErrorResponse toBody(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getStatus().value(), errorCode.name(), message);
    }
}
