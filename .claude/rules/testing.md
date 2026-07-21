---
description: Test conventions for Engage — QuarkusTest, RestAssured, mocked YouTube API
paths:
  - "src/test/java/**/*.java"
---

# Testing

- Use `@QuarkusTest` + RestAssured for HTTP API tests.
- Mock or stub the YouTube API in tests — never call live YouTube from CI.
- Use `%test` profile properties for `youtube.api.key` and similar config when needed.
- Tests should be self-contained; clean DB state per test when a test mutates data.
- Name tests with domain-scenario style (see `domain-model.md`): `shouldStripHtmlTagsBeforeTokenizing`,
  `shouldCountRepeatedWordsAndIgnoreStopWords`.
- Single-test run: `mvn test -Dtest=ClassName#methodName`.
