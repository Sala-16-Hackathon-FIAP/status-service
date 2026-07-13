package br.com.fiapx.status.infrastructure.rest;

import br.com.fiapx.status.application.port.input.StatusUseCase;
import br.com.fiapx.status.domain.exception.JobStatusNotFoundException;
import br.com.fiapx.status.domain.model.JobStatus;
import br.com.fiapx.status.domain.model.JobStatusType;
import br.com.fiapx.status.infrastructure.rest.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatusController.class)
@Import({GlobalExceptionHandler.class, br.com.fiapx.status.infrastructure.security.SecurityConfig.class})
class StatusControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean StatusUseCase statusUseCase;
    @MockBean br.com.fiapx.status.infrastructure.security.JwtAuthFilter jwtAuthFilter;

    private UUID userId;
    private UUID uploadId;
    private JobStatus sampleStatus;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();
        uploadId = UUID.randomUUID();
        sampleStatus = new JobStatus(UUID.randomUUID(), UUID.randomUUID(), uploadId, userId,
                "video.mp4", JobStatusType.PROCESSING_COMPLETED, "processed/key.zip", null,
                LocalDateTime.now(), LocalDateTime.now());
        doAnswer(invocation -> {
            var chain = invocation.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    private UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void listStatuses_shouldReturnUserStatuses() throws Exception {
        when(statusUseCase.getUserStatuses(userId)).thenReturn(List.of(sampleStatus));

        mockMvc.perform(get("/api/v1/status").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PROCESSING_COMPLETED"))
                .andExpect(jsonPath("$[0].filename").value("video.mp4"));
    }

    @Test
    void listStatuses_shouldReturnEmptyList_whenNoStatuses() throws Exception {
        when(statusUseCase.getUserStatuses(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/status").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getStatus_shouldReturnStatus_whenFound() throws Exception {
        when(statusUseCase.getStatusByUploadId(eq(uploadId), any())).thenReturn(sampleStatus);

        mockMvc.perform(get("/api/v1/status/uploads/{id}", uploadId)
                        .with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value(uploadId.toString()))
                .andExpect(jsonPath("$.resultS3Key").value("processed/key.zip"));
    }

    @Test
    void getStatus_shouldReturn404_whenNotFound() throws Exception {
        when(statusUseCase.getStatusByUploadId(any(), any()))
                .thenThrow(new JobStatusNotFoundException(UUID.randomUUID()));

        mockMvc.perform(get("/api/v1/status/uploads/{id}", UUID.randomUUID())
                        .with(authentication(userAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void listStatuses_shouldReturn403_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/status"))
                .andExpect(status().isForbidden());
    }
}
