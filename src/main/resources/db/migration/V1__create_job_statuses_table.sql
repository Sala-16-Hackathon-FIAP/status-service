CREATE TABLE IF NOT EXISTS job_statuses (
    id            UUID PRIMARY KEY,
    job_id        UUID,
    upload_id     UUID NOT NULL UNIQUE,
    user_id       UUID NOT NULL,
    filename      VARCHAR(500),
    status        VARCHAR(30) NOT NULL,
    result_s3_key VARCHAR(1000),
    error_message TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_job_statuses_user_id   ON job_statuses(user_id);
CREATE INDEX idx_job_statuses_upload_id ON job_statuses(upload_id);
CREATE INDEX idx_job_statuses_status    ON job_statuses(status);
