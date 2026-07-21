---
description: Local dev setup, Flyway/dev-import safety, tooling constraints, and the finish-checklist
---

# Development workflow

## Run locally

```bash
export YOUTUBE_API_KEY=your-key-here
mvn quarkus:dev
```

- Dev port: **8082** (test port is 8080 — don't mix them up).
- Flyway clean-at-start reloads schema and seeds channel `UC6g6eok10NJGYgenHO-0Oew` on every restart.
- The scheduler syncs videos/comments automatically once a channel has a valid API key.

Running alongside the platform: from `backoffice/`, `./scripts/dev-platform.sh` starts Passport + Visita +
Backoffice; add Engage manually on port 8082. Backoffice proxies `/engage/api/**` → port 8082.

When adding fields sourced from YouTube: add the migration and update the sync mapping together. Verify via
`/openapi` or a curl against `http://localhost:8082`.

**Never** commit a real `youtube.api.key`. Never ship a sync feature without documenting its required API key
in `ARCHITECTURE.md` §13.

## Migrations

Engage uses Flyway only — there's no `dev-import.sql` yet (the migration seed is the dev baseline). If one is
added later, treat it like the sibling Passport repo's dev-import pattern (same PR as the migration).

When changing schema:
1. Update entity fields and repositories in the same PR.
2. Update sync tasks if new `NOT NULL` columns need YouTube mapping.
3. Restart `mvn quarkus:dev` and confirm Flyway applies cleanly.

## Tooling constraints

- No Python anywhere in this repo.
- If scripts are added, bash or JBang only.
- No Node build — Java/Quarkus only.

## Finish checklist

After Java or SQL migration changes (skip for docs-only edits):

```bash
mvn -B clean test        # compiles, formats (process-sources), and runs tests
mvn -B clean compile     # faster compile-only check
mvn -B process-sources   # formatting only
mvn -B pmd:check         # bug-prone-pattern lint (resources/pmd-ruleset.xml) — see below
```

Don't consider a task done while `mvn clean compile` fails, or while new lint/diagnostic errors exist in
touched files.

## Linting (PMD)

`mvn pmd:check` runs PMD against `resources/pmd-ruleset.xml` (bestpractices + errorprone categories, with a
few rules excluded because they're noisy false positives in this codebase's idiom — see comments in the
ruleset file: `ConstantsInInterface`, `UnusedPrivateMethod`, `LooseCoupling`, `GuardLogStatement`,
`NullAssignment`, `MissingStaticMethodInNonInstantiatableClass`). `category/java/design.xml` (God class,
cyclomatic complexity, Law of Demeter, etc.) is deliberately not included — those concerns are handled by
`.claude/rules/domain-model.md` and code review instead, since they're too subjective for a hard gate.

The command exits non-zero on any violation (report at `target/pmd.xml`) and is not bound to the default
build lifecycle, so it won't slow down `mvn test`/CI — run it explicitly when you want lint feedback. It also
prints internal PMD type-resolution stack traces to stdout on some files (a known PMD 7 cosmetic quirk, not a
build failure) — look for the final `[ERROR] ... has found N violations` line or check `target/pmd.xml` for
the actual results.

As of this writing there are 21 pre-existing violations (mostly `InvalidLogMessageFormat` — SLF4J calls with
a mismatched placeholder/argument count) that haven't been cleaned up yet; don't let those block unrelated
work, but do fix them if you're already touching the same file.
