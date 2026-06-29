# Engage — Architecture

YouTube engagement service: channel registry, video/comment sync, live statistics via YouTube Data API.

## Stack

- Quarkus 3, PostgreSQL, Flyway, SmallRye JWT, Google YouTube API v3
- Dev port: **8082** (`%dev.quarkus.http.port`)
- API base: `/api`

## Key routes

| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/api/statistics` | `engage.admin` | Live YouTube stats for connected channels and synced videos |
| GET | `/api/channels` | `engage.admin` | List registered channels (no API key exposed) |
| POST | `/api/channels` | `engage.admin` | Register channel with optional `youtubeApiKey` and `connected` |
| GET | `/api/channels/{id}` | `engage.admin` | Channel by id |
| PUT | `/api/channels/{id}` | `engage.admin` | Update YouTube id, API key, or connected flag |
| DELETE | `/api/channels/{id}` | `engage.admin` | Remove channel |
| GET | `/api/videos` | `engage.admin` | List synced videos |
| GET | `/api/videos/{videoId}/comments` | `engage.admin` | Comments for one video (`ListVideoCommentsEndpoint`) |
| GET | `/api/channels/{channelId}/comments` | `engage.admin` | All comments for a channel's videos (`ListChannelCommentsEndpoint`) |

## YouTube API facade

All YouTube Data API v3 calls go through `YoutubeApiFacade` (`shared/youtube/`). Each method receives the **channel-specific API key**; sync and statistics skip channels that are not `connected` or lack a key.

Native image: register Google/YouTube JSON types and {@code YouTubeRequest} query-parameter fields via `GoogleApiReflectionConfig` (`@RegisterForReflection`). Without {@code YouTubeRequest}, {@code UriTemplate} cannot read the {@code key} field and YouTube returns 403 "unregistered callers" even when {@code curl} with the same key works.

Quota-conscious sync (configurable in `application.properties`):

| Property | Default | Effect |
|----------|---------|--------|
| `engage.sync.video.interval` | `5m` | Scheduler cadence |
| `engage.sync.video.pages-per-run` | `1` | Max search pages (100 videos) per channel per run |
| `engage.sync.comments.interval` | `10m` | Comment scheduler cadence |
| `engage.sync.comments.videos-per-run` | `3` | Videos processed per run (one comment page each) |

Video sync rotates one connected channel per run. Comment sync picks the oldest `comments_sync_at` and paginates via `comments_next_page_token`.

## Sync notifications (Passport)

Background sync tasks build a **sync run report** (one notification per run, one item per YouTube API call) and publish to Passport:

```properties
quarkus.rest-client.passport-api.url=${PASSPORT_API_URL:http://localhost:8080}
passport.internal.service-key=${PASSPORT_INTERNAL_SERVICE_KEY:dev-internal-service-key}
```

Package: `dev.vepo.engage.shared.notification` (`SyncRunReport`, `PassportNotificationClient`, `PassportNotificationPublisher`).

## Configuration

Per-channel `youtubeApiKey` is stored in `tb_channels` when registering/updating a **connected** channel. Optional global fallback env (legacy):

```properties
%dev.quarkus.http.port=8082
youtube.api.key=${YOUTUBE_API_KEY:}
```

## Development

```bash
# Register a connected channel via API (recommended)
curl -X POST http://localhost:8082/api/channels \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"youtubeId":"UC6g6eok10NJGYgenHO-0Oew","youtubeApiKey":"your-key","connected":true}'

./mvnw quarkus:dev
```

Seed channel in `V0.0.1__Database_Creation.sql` (`UC6g6eok10NJGYgenHO-0Oew`) is **disconnected** until an API key is set.

## CI

`.github/workflows/maven.yml`: `mvn clean compile`, `mvn test`. Native Docker image `vepo/engage` on push to `main` / tags (`src/main/docker/Dockerfile`).

## Consumers

- **Backoffice** — `/engage/statistics`; proxy `/engage/api/**` → Engage:8082
- **Passport** — receives sync run notifications via internal REST API
