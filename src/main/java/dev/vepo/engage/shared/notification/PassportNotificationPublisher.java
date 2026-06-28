package dev.vepo.engage.shared.notification;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PassportNotificationPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PassportNotificationPublisher.class);
    private static final String SERVICE_KEY_PROPERTY = "passport.internal.service-key";

    private final PassportNotificationClient passportNotificationClient;

    @Inject
    public PassportNotificationPublisher(@RestClient PassportNotificationClient passportNotificationClient) {
        this.passportNotificationClient = passportNotificationClient;
    }

    public void publishSyncReport(SyncRunReport report) {
        if (report == null) {
            return;
        }
        var serviceKey = ConfigProvider.getConfig()
                                       .getOptionalValue(SERVICE_KEY_PROPERTY, String.class)
                                       .filter(key -> !key.isBlank())
                                       .orElse(null);
        if (serviceKey == null) {
            logger.warn("Skipping sync notification: {} is not configured", SERVICE_KEY_PROPERTY);
            return;
        }
        try {
            passportNotificationClient.publishNotification(serviceKey, report.toPublishRequest());
        } catch (Exception ex) {
            logger.error("Failed to publish sync notification for channel {}", report.toPublishRequest().engageChannelId(), ex);
        }
    }
}
