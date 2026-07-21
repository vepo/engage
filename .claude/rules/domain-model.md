---
description: Domain model naming, Tell-Don't-Ask, and Law of Demeter for Engage Java code
paths:
  - "src/main/java/**/*.java"
  - "src/test/java/**/*.java"
---

# Domain model conventions

The domain specification lives in `docs/domain-specification.md` and must contain a "Ubiquitous Language"
section. Follow it together with `ARCHITECTURE.md`.

Before a domain-affecting change: read the spec, question the change against it (new entities, sync rules,
YouTube fields, API labels?), and update the spec **first** if the domain changes — don't write code or tests
before that's settled.

## Naming

Identifiers must use domain terms — **Channel**, **Video**, **Comment**, **Sync**, **YouTube channel id** — not
generic names like `ChannelData`, `fetchItems`, `ytObj`. Refactor technical names to domain terms when touching
violating code.

Test names use domain-scenario style: `shouldRegisterChannelWithYoutubeId`, `shouldListCommentsForVideo`.

## Tell, Don't Ask

Tell sync tasks and endpoints to **register channel**, **sync comments** — avoid scattered `setYoutubeId` calls
plus duplicate existence checks; consolidate that logic in repository or channel helper methods when touching
that code.

## Law of Demeter

Avoid train wrecks in business logic (`comment.getVideo().getChannel().getYoutubeId()`). Prefer repository
queries with `JOIN FETCH`, or expose helper methods on entities when multiple callers need the same hop.
Repository-internal JPQL paths are fine.
