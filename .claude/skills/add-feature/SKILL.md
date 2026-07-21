---
name: add-feature
description: Walk through Engage's feature workflow (ARCHITECTURE.md §12) for adding a new YouTube-ingestion or API feature — migration through docs update. Use when starting a new feature that touches the data model, sync, or a new endpoint.
---

Guide the implementation of a new feature through Engage's standard workflow, in this order. Confirm each
step with the user (or move on automatically if the step obviously doesn't apply, e.g. no new persisted data
means no migration) before starting the next:

1. **Read first** — read `ARCHITECTURE.md` and `docs/domain-specification.md` (Ubiquitous Language). If the
   feature introduces a new domain concept, sync rule, or YouTube field, update the domain spec **before**
   writing any code.

2. **Migration** — if the feature needs new/changed persisted data, add a Flyway migration under
   `src/main/resources/db/migration/`, following the existing `V0.0.x__Description.sql` naming pattern.

3. **Model** — add or update the JPA entity in `src/main/java/dev/vepo/engage/model/`, using domain
   terminology (see `.claude/rules/domain-model.md`) and `Instant` for timestamps.

4. **Repository** — add query methods to the relevant `*Repository` class. JPQL with named parameters,
   `Optional` for single results, no business logic (see `.claude/rules/java-style.md`).

5. **Endpoint** (if user-facing) — add the `@ApplicationScoped` `@Path` endpoint, record-based DTOs, no
   `EntityManager` — map to repository/service calls only.

6. **Sync task** (if this feature ingests YouTube data) — extend `YoutubeApiFacade` if the API shape changed,
   then the relevant `Sync*Task` for the workflow. Follow `.claude/rules/youtube-sync.md` for pagination and
   upsert conventions.

7. **Tests** — add tests under `src/test/java/...` mirroring the main package. Use `@QuarkusTest` +
   RestAssured for endpoints, mock/stub the YouTube API, domain-scenario test names (see
   `.claude/rules/testing.md`).

8. **Domain spec** — update `docs/domain-specification.md` if concepts, invariants, or terminology changed
   during implementation (not just planned upfront).

9. **ARCHITECTURE.md** — update routes, tables, sync jobs, or package descriptions to reflect the new feature.

10. Run the `verify` skill (or `mvn -B clean test` + `mvn -B pmd:check`) before considering the feature done.
