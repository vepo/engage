package dev.vepo.engage.shared.notification;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SyncReportFields {

    private SyncReportFields() {}

    public static Map<String, Object> apiCall(String operation,
                                              String status,
                                              int httpStatus,
                                              int itemCount,
                                              String channelId,
                                              String videoId,
                                              String error) {
        var fields = new LinkedHashMap<String, Object>();
        fields.put("operation", operation);
        fields.put("status", status);
        fields.put("httpStatus", httpStatus);
        fields.put("itemCount", itemCount);
        if (channelId != null) {
            fields.put("youtubeChannelId", channelId);
        }
        if (videoId != null) {
            fields.put("youtubeVideoId", videoId);
        }
        if (error != null) {
            fields.put("error", error);
        }
        return fields;
    }
}
