---
description: Java formatting, import order, string building, and JPA query style
paths:
  - "**/*.java"
---

# Java style

## Formatting and imports

Formatting is enforced by the Eclipse formatter (`resources/formatter.xml`) via `formatter-maven-plugin`, bound
to the Maven `process-sources` phase — `mvn compile`/`test`/`package` reformats sources automatically. After
editing Java by hand, run `mvn -B process-sources` and remove any unused imports yourself (the plugin formats,
it doesn't prune imports).

Import order: `java.*` / `jakarta.*` → `io.quarkus.*` → third-party → `dev.vepo.engage.*`.

## Conventions

- Prefer `var` when the type is obvious from the right-hand side.
- Use records for request/response DTOs (`CreateChannelRequest`, `ChannelResponse`).
- SLF4J for logging — never `System.out.println`, never log API keys.
- Entities live in `model/`; use `Instant` for timestamps.

## Strings

Use `.formatted()` for messages and exceptions — no `+` concatenation for interpolated strings. Fix legacy `+`
concatenation in `ChannelResource` when touching those lines.

## JPA / repositories

Queries only live in `*Repository` classes:

- Static lookups → JPQL (e.g. `FROM Channel WHERE youtubeId = :youtubeId`) with named parameters, always.
- Native SQL is a last resort, and only with a comment explaining why.
- No `EntityManager` in endpoints or `Sync*Task` — inject repositories instead.
