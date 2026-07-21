# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Engage: a Quarkus 3.30.5 / Java 21 service (Maven, groupId `dev.vepo`) that syncs YouTube channel/video/comment
data via the YouTube Data API v3 and exposes it over REST. PostgreSQL + Flyway. Part of a larger ecosystem
(sibling services: Passport for auth, Backoffice as frontend) — those live in separate repos, not this checkout.

Read `ARCHITECTURE.md` and `docs/domain-specification.md` (Ubiquitous Language) before making structural or
domain changes, and update them when routes, tables, sync jobs, packages, or domain language change.

## Build, test, run

**There is no `mvnw` wrapper in this repo** — use plain `mvn` (do not run `./mvnw`, it doesn't exist here).

```bash
mvn -B clean test          # compile, auto-format (process-sources), run tests — the finish-checklist gate
mvn -B clean compile       # faster compile-only check
mvn -B process-sources     # format only, via resources/formatter.xml (Eclipse formatter)
mvn -B pmd:check           # lint (resources/pmd-ruleset.xml) — not bound to the default build, run explicitly
mvn test -Dtest=ClassName#methodName   # run a single test
export YOUTUBE_API_KEY=your-key-here && mvn quarkus:dev   # dev mode, port 8082 (not the default 8080)
```

- `quarkus.http.test-port` is 8080 (tests), dev port is 8082 — don't confuse the two.
- Dev-mode DB is wiped and reseeded on every restart (`%dev.quarkus.flyway.clean-at-start=true`), seeding one
  disconnected channel (`UC6g6eok10NJGYgenHO-0Oew`).
- `quarkus.http.cors.origins=*` is set unconditionally (not `%dev.`-scoped) — don't assume CORS is dev-only.
- CI (`.github/workflows/maven.yml`) runs `mvn clean compile` then `mvn test`; native image + Docker push
  (`vepo/engage`) happen on push to `main`.

## Project structure

Single-module Maven project, organized by feature package under `src/main/java/dev/vepo/engage/`:
`channel/`, `video/` (incl. `video/sync`), `comments/` (`list/`, `sync/`, `wordcloud/`), `statistics/`,
`model/` (JPA entities), `shared/youtube/` (`YoutubeApiFacade` — sole YouTube API gateway),
`shared/notification/` (Passport integration), `shared/json/`, `shared/security/`.

## Architecture rules (enforced by convention, not tooling)

Dependencies flow downward: `*Endpoint` / `Sync*Task` → `YoutubeApiFacade` (external) → YouTube API, and
→ `*Service` (when needed) → `*Repository` → database.

- No `EntityManager` outside `*Repository` classes; no HTTP types in `Sync*Task`.
- YouTube API calls only inside `YoutubeApiFacade` — never from endpoints or repositories.
- Queries only in `*Repository` classes, JPQL with named parameters; native SQL is a last resort with a comment.
- Never hard-code, log, or expose YouTube API keys.
- Feature workflow order (ARCHITECTURE.md §12): migration → model → repository → endpoint → sync task
  (if ingestion) → tests → domain spec → ARCHITECTURE.md.

See `.claude/rules/` for the full conventions (domain model naming, Java style, YouTube sync internals,
testing, dev workflow) — these load automatically when relevant files are touched.

## No Python, no Node

This repo is Java/Quarkus only. Any added scripts must be bash or JBang — never Python, never a Node build.
