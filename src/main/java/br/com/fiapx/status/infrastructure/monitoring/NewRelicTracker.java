package br.com.fiapx.status.infrastructure.monitoring;

import com.newrelic.api.agent.NewRelic;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class NewRelicTracker {

    public void trackStatusUpdate(UUID uploadId, String status) {
        NewRelic.getAgent().getInsights().recordCustomEvent("JobStatusUpdate", Map.of(
                "uploadId", uploadId.toString(), "status", status));
    }
}
