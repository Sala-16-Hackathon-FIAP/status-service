package br.com.fiapx.status.infrastructure.monitoring;

import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.Insights;
import com.newrelic.api.agent.NewRelic;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NewRelicTrackerTest {

    @Test
    void trackStatusUpdate_shouldRecordCustomEvent() {
        NewRelicTracker tracker = new NewRelicTracker();
        try (MockedStatic<NewRelic> mocked = mockStatic(NewRelic.class)) {
            Agent agent = mock(Agent.class);
            Insights insights = mock(Insights.class);
            mocked.when(NewRelic::getAgent).thenReturn(agent);
            when(agent.getInsights()).thenReturn(insights);

            tracker.trackStatusUpdate(UUID.randomUUID(), "PROCESSING_COMPLETED");

            verify(insights).recordCustomEvent(eq("JobStatusUpdate"), anyMap());
        }
    }
}
