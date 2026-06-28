package dev.vepo.engage.shared.notification;

import java.util.List;

public record PublishNotificationRequest(String sourceService,
                                         String sourceType,
                                         Long engageChannelId,
                                         String title,
                                         String description,
                                         String report,
                                         List<PublishNotificationItemRequest> items) {}
