package dev.vepo.engage.shared.notification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.api.client.json.gson.GsonFactory;

public class SyncRunReport {

    private static final String SOURCE_SERVICE = "engage";

    private final String sourceType;
    private final Long engageChannelId;
    private final String title;
    private String description;
    private final Map<String, Object> summaryReport = new LinkedHashMap<>();
    private final List<SyncRunReportItem> items = new ArrayList<>();
    private boolean failed;

    public SyncRunReport(String sourceType, Long engageChannelId, String title, String description) {
        this.sourceType = sourceType;
        this.engageChannelId = engageChannelId;
        this.title = title;
        this.description = description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void putSummary(String key, Object value) {
        summaryReport.put(key, value);
    }

    public void markFailed(String error) {
        this.failed = true;
        putSummary("status", "failed");
        putSummary("error", error);
    }

    public boolean isFailed() {
        return failed;
    }

    public void addItem(String title, String description, Map<String, Object> reportFields) {
        items.add(new SyncRunReportItem(title, description, toJson(reportFields)));
    }

    public PublishNotificationRequest toPublishRequest() {
        var itemRequests = items.stream()
                                .map(item -> new PublishNotificationItemRequest(item.title(), item.description(), item.report()))
                                .toList();
        return new PublishNotificationRequest(SOURCE_SERVICE,
                                              sourceType,
                                              engageChannelId,
                                              title,
                                              description,
                                              toJson(summaryReport),
                                              itemRequests);
    }

    private static String toJson(Map<String, Object> fields) {
        try {
            return GsonFactory.getDefaultInstance().toString(fields);
        } catch (Exception ex) {
            return "{}";
        }
    }
}
