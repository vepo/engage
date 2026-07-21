package dev.vepo.engage.video.sync;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import dev.vepo.engage.channel.ChannelRepository;
import dev.vepo.engage.model.Channel;
import dev.vepo.engage.model.Video;
import dev.vepo.engage.shared.notification.PassportNotificationPublisher;
import dev.vepo.engage.shared.notification.SyncRunReport;
import dev.vepo.engage.shared.youtube.PlaylistVideoPage;
import dev.vepo.engage.shared.youtube.YoutubeApiFacade;
import dev.vepo.engage.shared.youtube.YoutubeVideoSnippet;
import dev.vepo.engage.video.VideoRepository;

class SyncVideoTaskTest {

    @Test
    void shouldMarkBackfillCompletedWhenLastPageReached() {
        var channel = channelReadyForSync();
        var facade = new StubYoutubeApiFacade(List.of(new PlaylistVideoPage(List.of(snippet("v1")), null, true)));
        var publisher = new StubPassportNotificationPublisher();
        var task = newTask(facade, channel, publisher);

        task.syncVideosForNextChannel();

        assertThat(channel.isBackfillCompleted()).isTrue();
        assertThat(channel.getNextPageToken()).isNull();
        assertThat(publisher.published).isEmpty();
    }

    @Test
    void shouldPersistNextPageTokenWhileBackfillInProgress() {
        var channel = channelReadyForSync();
        var facade = new StubYoutubeApiFacade(List.of(new PlaylistVideoPage(List.of(snippet("v1")), "TOKEN_A", false)));
        var publisher = new StubPassportNotificationPublisher();
        var task = newTask(facade, channel, publisher);

        task.syncVideosForNextChannel();

        assertThat(channel.isBackfillCompleted()).isFalse();
        assertThat(channel.getNextPageToken()).isEqualTo("TOKEN_A");
        assertThat(publisher.published).isEmpty();
    }

    @Test
    void shouldNotReArmBackfillAfterItAlreadyCompleted() {
        // Regression test: once backfill has completed, a steady-state freshness check
        // must not persist the fetched page's nextPageToken, or the channel would be
        // pushed
        // back into backfill mode forever and never poll the newest page again.
        var channel = channelReadyForSync();
        channel.setBackfillCompleted(true);
        channel.setNextPageToken(null);
        var facade = new StubYoutubeApiFacade(List.of(new PlaylistVideoPage(List.of(snippet("newest")), "SOME_TOKEN", false)));
        var publisher = new StubPassportNotificationPublisher();
        var task = newTask(facade, channel, publisher);

        task.syncVideosForNextChannel();

        assertThat(facade.requestedPageTokens).containsExactly((String) null);
        assertThat(channel.isBackfillCompleted()).isTrue();
        assertThat(channel.getNextPageToken()).isNull();
        assertThat(publisher.published).isEmpty();
    }

    @Test
    void shouldOnlyPublishNotificationWhenSyncFails() {
        // A failing YouTube call should still result in exactly one published
        // notification,
        // carrying the failure — routine successful runs must stay silent (see the
        // three
        // tests above), so Passport's feed isn't flooded by every 5-minute poll.
        var channel = channelReadyForSync();
        var facade = new ThrowingYoutubeApiFacade();
        var publisher = new StubPassportNotificationPublisher();
        var task = newTask(facade, channel, publisher);

        task.syncVideosForNextChannel();

        assertThat(publisher.published).hasSize(1);
        assertThat(publisher.published.getFirst().isFailed()).isTrue();
        assertThat(publisher.published.getFirst().toPublishRequest().report()).contains("YouTube is down");
    }

    private SyncVideoTask newTask(YoutubeApiFacade facade, Channel channel, PassportNotificationPublisher publisher) {
        return new SyncVideoTask(facade, new StubChannelRepository(channel), new StubVideoRepository(), publisher, 1);
    }

    private Channel channelReadyForSync() {
        var channel = new Channel();
        channel.setId(1L);
        channel.setYoutubeId("UCabc123");
        channel.setYoutubeApiKey("api-key");
        channel.setConnected(true);
        channel.setUploadsPlaylistId("UUabc123");
        return channel;
    }

    private YoutubeVideoSnippet snippet(String youtubeId) {
        return new YoutubeVideoSnippet(youtubeId, "Title", "Description", "https://thumb", null);
    }

    private static class StubYoutubeApiFacade extends YoutubeApiFacade {
        private final List<PlaylistVideoPage> pages;
        private final List<String> requestedPageTokens = new ArrayList<>();
        private int callIndex;

        StubYoutubeApiFacade(List<PlaylistVideoPage> pages) {
            this.pages = pages;
        }

        @Override
        public PlaylistVideoPage fetchUploadsPlaylistPage(String apiKey, String uploadsPlaylistId, String pageToken,
                                                          SyncRunReport report) {
            requestedPageTokens.add(pageToken);
            return pages.get(Math.min(callIndex++, pages.size() - 1));
        }
    }

    private static class ThrowingYoutubeApiFacade extends YoutubeApiFacade {
        @Override
        public PlaylistVideoPage fetchUploadsPlaylistPage(String apiKey, String uploadsPlaylistId, String pageToken,
                                                          SyncRunReport report) {
            throw new IllegalStateException("YouTube is down");
        }
    }

    private static class StubChannelRepository extends ChannelRepository {
        private final Channel channel;

        StubChannelRepository(Channel channel) {
            super(null);
            this.channel = channel;
        }

        @Override
        public List<Channel> findConnectedReadyForSync() {
            return List.of(channel);
        }

        @Override
        public Channel merge(Channel channel) {
            return channel;
        }
    }

    private static class StubVideoRepository extends VideoRepository {
        private final List<Video> saved = new ArrayList<>();

        StubVideoRepository() {
            super(null);
        }

        @Override
        public Optional<Video> findByYoutubeId(String youtubeId) {
            return saved.stream().filter(v -> youtubeId.equals(v.getYoutubeId())).findFirst();
        }

        @Override
        public Video save(Video video) {
            saved.add(video);
            return video;
        }
    }

    private static class StubPassportNotificationPublisher extends PassportNotificationPublisher {
        private final List<SyncRunReport> published = new ArrayList<>();

        StubPassportNotificationPublisher() {
            super(null);
        }

        @Override
        public void publishSyncReport(SyncRunReport report) {
            published.add(report);
        }
    }
}
