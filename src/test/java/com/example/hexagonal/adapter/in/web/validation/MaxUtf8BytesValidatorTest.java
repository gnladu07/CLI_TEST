package com.example.hexagonal.adapter.in.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class MaxUtf8BytesValidatorTest {

    private static final String EMOJI = new String(Character.toChars(0x1F600));

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    record Target(@MaxUtf8Bytes(8) String value) {
    }

    @Test
    void UTF_8_바이트_수가_한도_이하이면_유효하다() {
        assertThat(validator.validate(new Target(EMOJI + EMOJI))).isEmpty();
        assertThat(validator.validate(new Target("abcdefgh"))).isEmpty();
    }

    @Test
    void UTF_8_바이트_수가_한도를_넘으면_유효하지_않다() {
        assertThat(validator.validate(new Target(EMOJI + EMOJI + "a"))).hasSize(1);
        assertThat(validator.validate(new Target("abcdefghi"))).hasSize(1);
    }

    @Test
    void null은_유효하다() {
        assertThat(validator.validate(new Target(null))).isEmpty();
    }
}
