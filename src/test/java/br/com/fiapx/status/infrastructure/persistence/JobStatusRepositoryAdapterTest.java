package br.com.fiapx.status.infrastructure.persistence;

import br.com.fiapx.status.domain.model.JobStatus;
import br.com.fiapx.status.domain.model.JobStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobStatusRepositoryAdapterTest {

    @Mock
    private JobStatusJpaRepository jpaRepository;

    private JobStatusRepositoryAdapter adapter;
    private JobStatus sampleStatus;

    @BeforeEach
    void setUp() {
        adapter = new JobStatusRepositoryAdapter(jpaRepository);
        sampleStatus = new JobStatus(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "video.mp4", JobStatusType.PROCESSING_COMPLETED,
                "res/key", null, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void save_shouldPersistAndReturnDomainObject() {
        JobStatusEntity entity = JobStatusEntity.fromDomain(sampleStatus);
        when(jpaRepository.save(any())).thenReturn(entity);

        JobStatus result = adapter.save(sampleStatus);

        assertThat(result.id()).isEqualTo(sampleStatus.id());
        verify(jpaRepository).save(any(JobStatusEntity.class));
    }

    @Test
    void findByUploadId_shouldReturnStatus() {
        JobStatusEntity entity = JobStatusEntity.fromDomain(sampleStatus);
        when(jpaRepository.findByUploadId(sampleStatus.uploadId())).thenReturn(Optional.of(entity));

        Optional<JobStatus> result = adapter.findByUploadId(sampleStatus.uploadId());

        assertThat(result).isPresent();
    }

    @Test
    void findByUploadId_shouldReturnEmpty_whenNotExists() {
        when(jpaRepository.findByUploadId(any())).thenReturn(Optional.empty());

        Optional<JobStatus> result = adapter.findByUploadId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_shouldReturnList() {
        JobStatusEntity entity = JobStatusEntity.fromDomain(sampleStatus);
        when(jpaRepository.findByUserId(sampleStatus.userId())).thenReturn(List.of(entity));

        List<JobStatus> result = adapter.findByUserId(sampleStatus.userId());

        assertThat(result).hasSize(1);
    }
}
