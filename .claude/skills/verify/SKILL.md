---
name: verify
description: Run Engage's finish-checklist (compile, format, test) and report failures or new lint issues in touched files. Use before considering a Java or SQL migration change done.
---

Run the project's finish checklist and report the results.

1. Run `mvn -B clean test` from the repo root. This compiles, runs the Eclipse formatter
   (`process-sources` phase, `resources/formatter.xml`), and runs the test suite.
2. If it fails, run `mvn -B clean compile` to isolate whether the failure is a compile error or a test
   failure, then report the specific error(s) with file:line references.
3. If it succeeds, check whether `mvn -B process-sources` changed any files (reformatting) — if so, note
   which files were reformatted.
4. Run `mvn -B pmd:check` (ruleset: `resources/pmd-ruleset.xml`). It's expected to exit non-zero — the repo
   currently has ~21 pre-existing violations not yet cleaned up. Compare violations in `target/pmd.xml`
   against files touched in this session: flag only NEW violations in touched files, don't re-report
   pre-existing ones in untouched code. Ignore the internal PMD stack traces it sometimes prints to stdout —
   that's a cosmetic PMD 7 quirk, not a failure.
5. Check IDE diagnostics on files touched in this session (equivalent to `ReadLints`) and report any new
   errors or warnings introduced by the change — don't report pre-existing issues in untouched code.
6. Summarize: pass/fail, what was reformatted, and any new PMD/diagnostic issues — do not mark the task done
   while `mvn clean compile` fails or new lint/PMD errors exist in touched files.

Skip this entirely for docs-only or `.md`/`.mdc`-only changes — nothing to compile or test.
