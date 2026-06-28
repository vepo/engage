package dev.vepo.engage.shared.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SyncRunReport Tests")
class SyncRunReportTest {

    @Test
    @DisplayName("Should build publish request with items")
    void toPublishRequest_IncludesItems() {
        var report = new SyncRunReport("video_sync", 1L, "Sincronização de vídeos", "Canal UC test");
        report.putSummary("status", "completed");
        report.addItem("youtube.search.list",
                       "Busca de vídeos",
                       SyncReportFields.apiCall("youtube.search.list", "success", 200, 3, "UC123", null, null));

        var request = report.toPublishRequest();

        assertEquals("engage", request.sourceService());
        assertEquals("video_sync", request.sourceType());
        assertEquals(1L, request.engageChannelId());
        assertEquals(1, request.items().size());
        assertFalse(request.items().getFirst().report().isBlank());
    }
}
