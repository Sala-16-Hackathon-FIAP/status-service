package br.com.fiapx.status.infrastructure.rest.handler;

import br.com.fiapx.status.domain.exception.JobStatusNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFound_shouldReturn404() {
        JobStatusNotFoundException ex = new JobStatusNotFoundException(UUID.randomUUID());
        ProblemDetail detail = handler.handleNotFound(ex);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void handleGeneral_shouldReturn500() {
        Exception ex = new RuntimeException("unexpected");
        ProblemDetail detail = handler.handleGeneral(ex);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(detail.getDetail()).isEqualTo("An unexpected error occurred");
    }
}
