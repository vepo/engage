---
description: YoutubeApiFacade and Sync*Task conventions for YouTube Data API ingestion
paths:
  - "src/main/java/**/youtube/**/*.java"
  - "src/main/java/**/sync/**/*.java"
---

# YouTube integration

## YoutubeApiFacade

The single gateway to the YouTube Data API v3 — every external call goes through it.

- Every method takes the channel-specific API key (`setKey(apiKey)` on requests) — channels carry their own
  `youtubeApiKey` once connected.
- Paginate with `nextPageToken`; expose page-level methods for quota-aware sync
  (`fetchUploadsPlaylistPage`, `fetchCommentThreadPage`, …).
- Handle `GoogleJsonResponseException` here; log errors with channel/video id context.
- On FORBIDDEN for search/comments, return the last page rather than failing the whole sync.

## SyncVideoTask

- One connected channel per scheduled run, round-robin.
- One uploads-playlist page per run (`engage.sync.video.pages-per-run`, default 1); backfill continues via
  `nextPageToken` into older videos.
- Upsert videos by `youtubeId`; update the channel's `syncAt` and `nextPageToken`.

## SyncCommentsTask

- Process up to `engage.sync.comments.videos-per-run` videos due by `commentsSyncAt`.
- One comment page per video per run; store `commentsNextPageToken` until exhausted.
- Process top-level threads and replies; upsert by `youtubeCommentId`.

## Never

- Call the YouTube API from endpoints or repositories — only `YoutubeApiFacade` does that.
- Hard-code or expose API keys in responses or logs.
- Change scheduler intervals (`engage.sync.video.interval`, `engage.sync.comments.interval`) without noting the
  quota impact in `ARCHITECTURE.md`.
