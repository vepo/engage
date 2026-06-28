package dev.vepo.engage.shared.notification;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PassportNotificationPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PassportNotificationPublisher.class);
    private static final String SERVICE_KEY_HEADER = "X-Service-Key";

    private final PassportNotificationClient passportNotificationClient;
    private final String serviceKey;

    @Inject
    public PassportNotificationPublisher(@RestClient PassportNotificationClient passportNotificationClient,
                                         @ConfigProperty(name = "passport.internal.service-key") String serviceKey) {
        this.passportNotificationClient = passportNotificationClient;
        this.serviceKey = serviceKey;
    }

    public void publishSyncReport(SyncRunReport report) {
        if (report == null) {
            return;
        }
        try {
            passportNotificationClient.publishNotification(serviceKey, report.toPublishRequest());
        } catch (Exception ex) {
            logger.error("Failed to publish sync notification for channel {}", report.toPublishRequest().engageChannelId(), ex);
        }
    }
}
